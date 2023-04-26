package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.DAO.UserDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

@WebServlet(name = "Home", value = "/home")
public class Home extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    private Connection connection;

    public void init() throws UnavailableException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(this.application);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("WEB-INF/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);

        this.connection = ConnectionFactory.getConnection(getServletContext());
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        HttpSession session = request.getSession(false);
        ProductDAO productDAO = new ProductDAO(connection);

        User user = (User)session.getAttribute("user");
        Queue<Product> menuProducts, randomProducts;
        try {
            menuProducts = productDAO.getLastViewedProductsForUser( user.email() );
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving products for the menu");
            return;
        }

        if(menuProducts.size() < 5){

            try{
                randomProducts = productDAO.getFiveRandomProducts();
            }catch (SQLException e){
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving products for the menu");
                return;
            }

            while(!randomProducts.isEmpty() && menuProducts.size() < 5){
                Product p = randomProducts.poll();
                if(!menuProducts.contains(p)){
                    menuProducts.add(p);
                }
            }
        }

        ctx.setVariable("products", menuProducts);
        this.templateEngine.process("home",ctx, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //DO NOTHING
    }

    /**
     * Close the {@link Connection} to the database.
     */
    @Override
    public void destroy() {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException ignored) {
        }
    }

}
