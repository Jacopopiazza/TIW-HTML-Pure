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
import java.util.Comparator;
import java.util.List;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final IWebExchange webExchange = this.application.buildExchange(request, response);
        final WebContext ctx = new WebContext(webExchange, request.getLocale());

        HttpSession session = request.getSession(false);
        CartDAO cartDAO = new CartDAO(session, connection);
        OrderDAO orderDAO = new OrderDAO(connection);

        User user = (User)session.getAttribute("user");
        List<Product> menuProducts;
        try {
            menuProducts = new ProductDAO(connection).getMenuProductsForUser( user.email() );
        }catch (SQLException e){
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while retriving products for the menu");
            return;
        }

        ctx.setVariable("products", menuProducts);

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

        try{
            this.templateEngine.process("ordini",ctx, response.getWriter());
        }catch (Exception ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in rendering order page." + ex.getMessage());
            return;
        }

    }
        @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //Check params
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

        if(codiceFornitore < 0){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter");
            return;
        }

        //Create needed objects
        CartDAO cartDAO = new CartDAO(request.getSession(false), connection);
        ProductDAO productDAO = new ProductDAO(connection);
        SupplierDAO supplierDAO = new SupplierDAO(connection);
        Map<Integer, Map<Integer, Integer>> cart = cartDAO.getCart();

        //Supplier is not in cart
        if(!cart.containsKey(codiceFornitore)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No products in cart for this supplier, cannot create an order.");
            return;
        }

        Map<Integer,Integer> prodottiPerOrdine = cart.get(codiceFornitore);
        int finalCodiceFornitore = codiceFornitore;
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
                    return productDAO.getPriceForProductFromSupplier(x.getKey(), finalCodiceFornitore) * x.getValue();
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
            supplier = supplierDAO.getSupplier(codiceFornitore);
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
                speseSpedizione = supplierDAO.getDeliveryCostOfSupplierForNProducts(codiceFornitore, articoliNelCarrello);
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
            orderDAO.createOrder(user, codiceFornitore, speseSpedizione, subTotale, prodottiPerOrdine, supplier.getNome());
        }catch (SQLException ex){
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while creating the order\n." + ex.getMessage());
            return;
        }

        cartDAO.removeProductOfSupplier(codiceFornitore);

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
