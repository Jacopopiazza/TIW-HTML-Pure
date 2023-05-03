package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.DAO.CartDAO;
import it.polimi.tiw.tiw_html_pure.DAO.OrderDAO;
import it.polimi.tiw.tiw_html_pure.Utilities.ConnectionFactory;
import it.polimi.tiw.tiw_html_pure.Utilities.TemplateFactory;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@WebServlet(name ="Ordini", value = "/ordini")
public class Ordini extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String sCodiceFornitore = request.getParameter("codiceFornitore");

        if(sCodiceFornitore == null || sCodiceFornitore.isEmpty()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        int codiceFornitore = -1;
        try{
            codiceFornitore = Integer.parseInt(sCodiceFornitore);
        }catch (NumberFormatException ex){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Inavlid parameter");
            return;
        }

        CartDAO cartDAO = new CartDAO(request.getSession(false), connection);
        Map<Integer, Map<Integer, Integer>> cart = cartDAO.getCart();

        if(!cart.containsKey(codiceFornitore)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        OrderDAO orderDAO = new OrderDAO(connection);

        try{
            orderDAO.createOrder();
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating the order");
            return;
        }


        String path = getServletContext().getContextPath() + "/ordini";
        response.sendRedirect(path);


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
