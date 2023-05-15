package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name="ProdottoVisualizzato", value="/viewProduct")
public class ViewProduct extends HttpServlet {

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
     * Handles the POST request coming from a user's interaction with a product visualization, by marking the product as viewed by
     * the user and returning to the search results page, filtered by the opened products.
     *
     * @param req the HttpServletRequest object containing the information sent by the client.
     * @param resp the HttpServletResponse object used to send data back to the client.
     * @throws IOException if an error occurs during the process of sending the response to the client.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String codiceVisualizzato = req.getParameter("visualizzato");
        String[] aperti = req.getParameterValues("aperto");
        String queryString = req.getParameter("queryString");

        if(codiceVisualizzato == null || codiceVisualizzato.isEmpty() || queryString == null || queryString.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "codiceVisualizzato cannot be empty");
            return;
        }

        int idProductVisualizzato;

        try {
            idProductVisualizzato = Integer.parseInt(codiceVisualizzato);
            if(aperti != null){
                for(String s : aperti){
                    Integer.parseInt(s);
                }
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter in report creation");
            return;
        }

        if(idProductVisualizzato < 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (User)session.getAttribute("user");
        ProductDAO productDAO = new ProductDAO(connection);

        try{
            productDAO.markProductAsViewdByUser(user, idProductVisualizzato);
        }catch(SQLException ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while setting product viewed");
            return;
        }

        String path = getServletContext().getContextPath() + "/results";

        path += "?queryString=" + queryString;

        path += "&aperto=" + idProductVisualizzato;

        if(aperti != null)
            for(String s : aperti){
                path += "&aperto=" + s;
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
