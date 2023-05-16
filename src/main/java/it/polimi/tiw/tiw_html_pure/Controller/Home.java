package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
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
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "Home", value= {"/home"})
public class Home extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private Connection connection;

    /**
     * Initializes the servlet by building the application and getting a connection to the database.
     * @throws UnavailableException if the servlet is unable to initialize properly.
     */
    public void init() throws UnavailableException {
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = TemplateFactory.getTemplateEngine(this.application);
        this.connection = ConnectionFactory.getConnection(getServletContext());
    }

    /**
     * Handles the GET request for the home page. Retrieves the last five viewed products for the user
     * from the database and sets them as a request attribute to be displayed in the view. Renders the
     * view using Thymeleaf template engine.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws IOException if there is an I/O problem
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        ProductDAO productDAO = new ProductDAO(connection);

        User user = (User)session.getAttribute("user");
        List<Product> lastViewedProducts;
        try {
            lastViewedProducts = productDAO.getLastFiveViewedProductsForUser( user.email() );
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving products for the menu");
            return;
        }

        ctx.setVariable("products", lastViewedProducts);

        response.setCharacterEncoding("UTF-8");

        try{
            this.templateEngine.process("home",ctx, response.getWriter());
        }catch (Exception ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in rendering template");
        }

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
