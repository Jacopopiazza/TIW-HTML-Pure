package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.UserDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name = "Login", value = "/login")
public class Login extends HttpServlet {
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
     *
     * This method handles the GET request to display the login page.
     * If the request contains a "error" parameter, it sets the "error" variable in the context to true.
     * If the request contains a "success" parameter, it sets the "success" variable in the context to true.
     *
     * @param request the HttpServletRequest object that contains the request the client has made of the servlet
     * @param response the HttpServletResponse object that contains the response the servlet sends to the client
     * @throws IOException if an input or output error is detected when the servlet handles the GET request
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        if (request.getParameter("error") != null)
            ctx.setVariable("error", true);
        else if (request.getParameter("success") != null)
            ctx.setVariable("success", true);

        response.setCharacterEncoding("UTF-8");
        this.templateEngine.process("login",ctx, response.getWriter());
    }

    /**
     *
     * Handles the login form submission. Validates the email and password parameters, checks the credentials against the
     * UserDAO, sets the authenticated user in the session if the credentials are valid, and redirects the user to the home
     * page. If the email or password parameters are missing or invalid, or if the credentials are invalid, the method
     * sends an error response with an appropriate message.
     *
     * @param req the HttpServletRequest object containing the login form data
     *
     * @param resp the HttpServletResponse object used to send the response back to the client
     *
     * @throws IOException if an I/O error occurs while processing the request or response
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email or password cannot be empty");
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user = null;
        try {
            if (UserDAO.isValidEmail(email) && userDAO.doesEmailExist(email)) {
                user = userDAO.checkCredentials(email, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while checking credentials");
            return;
        }

        String path;
        if (user == null)
            path = getServletContext().getContextPath() + "/login?error=true";
        else {
            req.getSession().setAttribute("user", user);
            path = getServletContext().getContextPath() + "/home";
        }
        resp.sendRedirect(path);
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
