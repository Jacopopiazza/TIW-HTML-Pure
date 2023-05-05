package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.Supplier;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.CartDAO;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.DAO.SupplierDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.standard.processor.StandardInlineEnablementTemplateBoundariesProcessor;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name ="Risultati", value = "/risultati", loadOnStartup = 1)
public class Risultati extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private Connection connection;

    public void init() throws UnavailableException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateFactory.getTemplateEngine(this.application);
        this.connection = ConnectionFactory.getConnection(getServletContext());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        //Check param is there
        String queryString = request.getParameter("queryString");
        if (queryString == null || queryString.isEmpty()) {
            response.sendRedirect(getServletContext().getContextPath() + "/home");
            return;
        }

        SupplierDAO supplierDAO = new SupplierDAO(connection);

        String[] aperti = request.getParameterValues("aperto");
        Map<Integer, Map<Supplier, Integer>> prodottiAperti = new HashMap<>();
         if(aperti != null){
            for(String s : aperti)
                try{
                    int codice = Integer.parseInt(s);
                    prodottiAperti.put(codice, supplierDAO.getSuppliersAndPricesForProduct(codice));
                }catch (NumberFormatException ex){
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
                    return;
                }catch(SQLException ex){
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving suppliers for open products" + ex.getMessage());
                    return;
                }
        }


        //Get products for menu
        HttpSession session = request.getSession(false);
        User user = (User)session.getAttribute("user");
        ProductDAO productDAO = new ProductDAO(connection);

        List<Product> menuProducts;
        try {
            menuProducts = productDAO.getMenuProductsForUser( user.email() );
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving products for the menu");
            return;
        }

        //Get results of search
        Map<Product, Double> risultati;

        try{
            risultati = productDAO.getProductsFromQueryString(queryString);
        } catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure of search of products in database");
            return;
        }

        if(menuProducts == null || risultati == null){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failure of search of products in database");
            return;
        }

        if(!risultati.keySet().stream().map(x -> x.codice()).toList().containsAll(prodottiAperti.keySet())){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid opened product");
            return;
        }

        //
        CartDAO cartDAO = new CartDAO(request.getSession(false), this.connection);

        ctx.setVariable("cartDAO", cartDAO);
        ctx.setVariable("risultati", risultati);
        ctx.setVariable("products", menuProducts);
        ctx.setVariable("prodottiAperti" , prodottiAperti);
        ctx.setVariable("queryString", queryString);

        response.setCharacterEncoding("UTF-8");
        try{
            this.templateEngine.process("risultati",ctx, response.getWriter());
        }catch (Exception e){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }
    }

    @Override
    public void destroy() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException ignored) {
        }
    }
}
