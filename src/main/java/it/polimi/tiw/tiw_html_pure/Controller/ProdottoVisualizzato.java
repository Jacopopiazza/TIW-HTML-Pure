package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.DAO.UserDAO;
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

@WebServlet(name="ProdottoVisualizzato", value="/visualizza")
public class ProdottoVisualizzato extends HttpServlet {

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String codiceVisualizzato = req.getParameter("visualizzato");
        String[] aperti = req.getParameterValues("aperto");
        String queryString = req.getParameter("queryString");

        if(codiceVisualizzato == null || codiceVisualizzato.isEmpty() || queryString == null || queryString.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "codiceVisualizzato cannot be empty");
            return;
        }

        int codiceProdottoVisualizzato;

        try {
            codiceProdottoVisualizzato = Integer.parseInt(codiceVisualizzato);
            if(aperti != null){
                for(String s : aperti){
                    Integer.parseInt(s);
                }
            }
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad parameter in report creation");
            return;
        }

        if(codiceProdottoVisualizzato < 0){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        HttpSession session = req.getSession(false);
        User user = (User)session.getAttribute("user");
        ProductDAO productDAO = new ProductDAO(connection);

        try{
            productDAO.prodottoVisualizzato(user, codiceProdottoVisualizzato);
        }catch(SQLException ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while setting product viewed");
            return;
        }

        String path = getServletContext().getContextPath() + "/risultati";

        path += "?queryString=" + queryString;

        path += "&aperto=" + codiceProdottoVisualizzato;

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
