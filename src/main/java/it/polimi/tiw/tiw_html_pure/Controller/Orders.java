package it.polimi.tiw.tiw_html_pure.Controller;

import it.polimi.tiw.tiw_html_pure.Bean.*;
import it.polimi.tiw.tiw_html_pure.DAO.CartDAO;
import it.polimi.tiw.tiw_html_pure.DAO.OrderDAO;
import it.polimi.tiw.tiw_html_pure.DAO.ProductDAO;
import it.polimi.tiw.tiw_html_pure.DAO.SupplierDAO;
import it.polimi.tiw.tiw_html_pure.InvalidParameterException;
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
import java.util.Map;

@WebServlet(name ="Ordini", value = "/orders")
public class Orders extends HttpServlet {
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
     * Handles GET requests for the orders page. Retrieves all the orders from the database for the currently logged-in user,
     * and passes them to the Thymeleaf template engine for rendering.
     *
     * @param request the HttpServletRequest object containing the request from the client
     *
     * @param response the HttpServletResponse object for sending the response to the client
     *
     * @throws IOException if an error occurs while writing the response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        OrderDAO orderDAO = new OrderDAO(connection);

        User user = (User)session.getAttribute("user");

        List<Order> ordini;

        try {
            ordini = orderDAO.getOrdersForUser(user.email());
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in fetching orders for logged user from db.\n" + ex.getMessage());
            return;
        }

        if(ordini == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in fetching orders for logged user from db.");
            return;
        }


        ctx.setVariable("ordini", ordini);
        ctx.setVariable("supplierDAO", new SupplierDAO(connection));
        ctx.setVariable("productDAO", new ProductDAO(connection));

        response.setCharacterEncoding("UTF-8");
        try{
            this.templateEngine.process("ordini",ctx, response.getWriter());
        }catch (Exception ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in rendering order page." + ex.getMessage());
            return;
        }

    }

    /**
     * Handles the creation of a new order for a specific supplier based on the current user's cart.
     * The method retrieves the products added to the cart by the user for the specified supplier and
     * calculates the total cost of the order, including any delivery costs that may apply. The method then
     * creates a new order and removes the products from the user's cart for that specific supplier.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @throws IOException if an I/O error occurs while handling the request
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setCharacterEncoding("UTF-8");

        //Check params
        String sidSupplier = request.getParameter("idSupplier");

        if(sidSupplier == null || sidSupplier.isEmpty()){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        int idSupplier = -1;
        try{
            idSupplier = Integer.parseInt(sidSupplier);
        }catch (NumberFormatException ex){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Inavlid parameter");
            return;
        }

        if(idSupplier < 0){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        //Create needed objects
        CartDAO cartDAO = new CartDAO(request.getSession(false), connection);
        ProductDAO productDAO = new ProductDAO(connection);
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        Map<Integer, Map<Integer, Integer>> cart = cartDAO.getCart();

        //Supplier is not in cart
        if(!cart.containsKey(idSupplier)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No products in cart for this supplier, cannot create an order.");
            return;
        }

        Map<Integer,Integer> prodottiPerOrdine = cart.get(idSupplier);
        int finalidSupplier = idSupplier;
        int subTotale = -1;
        int articoliNelCarrello = prodottiPerOrdine.values().stream().reduce(0, Integer::sum);
        int speseSpedizione = 0;
        Supplier supplier;

        if(articoliNelCarrello == 0){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No products in cart for this supplier, cannot create an order.");
            return;
        }

        //Calcuate total of the order
        try {
            subTotale =  prodottiPerOrdine.entrySet().stream().map(x -> {
                try {
                    return productDAO.getPriceForProductFromSupplier(x.getKey(), finalidSupplier) * x.getValue();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).reduce(0, Integer::sum);
        }catch (RuntimeException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving prices." + ex.getMessage());
            return;
        }

        //If total < 0 -> error
        if(subTotale < 0){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No products in cart for this supplier, cannot create an order.");
            return;
        }

        //Get supplier to calculate delivery costs
        try{
            supplier = supplierDAO.getSupplier(idSupplier);
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving supplier info." + ex.getMessage());
            return;
        }

        if(supplier == null){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving supplier info.");
            return;
        }

        //get eventual delivery costs
        if(supplier.getSogliaSpedizioneGratuita() == null ||  subTotale < supplier.getSogliaSpedizioneGratuita()){
            try{
                speseSpedizione = supplierDAO.getDeliveryCostOfSupplierForNProducts(idSupplier, articoliNelCarrello);
            }catch (SQLException ex){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving delivery cost info." + ex.getMessage());
                return;
            }catch (InvalidParameterException ex){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retriving delivery cost info. Some parameters to query where wrong." + ex.getMessage());
                return;
            }
        }

        //Create order
        OrderDAO orderDAO = new OrderDAO(connection);
        User user = (User)request.getSession(false).getAttribute("user");

        try{
            orderDAO.createOrder(user, idSupplier, speseSpedizione, subTotale, prodottiPerOrdine);
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating the order\n." + ex.getMessage());
            return;
        }

        cartDAO.removeProductOfSupplier(idSupplier);

        String path = getServletContext().getContextPath() + "/orders";
        response.sendRedirect(path);


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
