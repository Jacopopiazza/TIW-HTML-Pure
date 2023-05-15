package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.CartDAO;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.DAO.SupplierDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name ="Carrello", value = "/cart")
public class Cart extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private Connection connection;

    public void init() throws UnavailableException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateFactory.getTemplateEngine(this.application);
        this.connection = ConnectionFactory.getConnection(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        HttpSession session = request.getSession(false);
        ProductDAO productDAO = new ProductDAO(connection);

        User user = (User)session.getAttribute("user");


        CartDAO cartDAO = new CartDAO(session,connection);
        SupplierDAO supplierDAO = new SupplierDAO(connection);

        ctx.setVariable("cart", cartDAO.getCart());
        ctx.setVariable("supplierDAO", supplierDAO);
        ctx.setVariable("productDAO", productDAO);


        response.setCharacterEncoding("UTF-8");
        try{
            this.templateEngine.process("carrello",ctx, response.getWriter());
        }catch(RuntimeException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while rendering cart.");
            return;
        }

    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String sidSupplier = req.getParameter("idSupplier"),
                sidProduct = req.getParameter("idProduct"),
                samount = req.getParameter("amount");


        if(sidSupplier == null || sidSupplier.isEmpty() ||
                sidProduct == null || sidProduct.isEmpty() ||
                samount == null || samount.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter provided");
            return;
        }
        int idSupplier, idProduct, amount;

        try{
            idSupplier = Integer.parseInt(sidSupplier);
            idProduct = Integer.parseInt(sidProduct);
            amount = Integer.parseInt(samount);
        }catch(NumberFormatException ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        if(amount <= 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        ProductDAO productDAO = new ProductDAO(connection);

        try{
            if(!productDAO.checkProductHasSupplier(idProduct,idSupplier)){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
                return;
            }
        }catch (SQLException ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occured while checking parameter");
            return;
        }

        CartDAO cartDAO = new CartDAO(req.getSession(false), this.connection);
        cartDAO.addProductToCart(idProduct, idSupplier, amount);

        String path = getServletContext().getContextPath() + "/cart";
        resp.sendRedirect(path);
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
