package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.InfoSupplier;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CartDAO {

    private final HttpSession session;
    private final Connection connection;

    public CartDAO(HttpSession session, Connection connection) {
        this.session = session;
        this.connection = connection;
    }

    public void addProductToCart(int idProduct, int idSupplier, int amount){

        Map<Integer, Map<Integer, Integer>> carrello = getRealCart();

        int old = 0;

        if(!carrello.containsKey(idSupplier)){
            carrello.put(idSupplier, new HashMap<>());
        }

        if(carrello.get(idSupplier).containsKey(idProduct)){
            old = carrello.get(idSupplier).get(idProduct);
        }

        carrello.get(idSupplier).put(idProduct, old + amount);

    }

    public InfoSupplier getInformationForSupplier(int idSupplier) {

        ProductDAO productDAO = new ProductDAO(connection);
        Map<Integer, Map<Integer, Integer>> carrello = getRealCart();
        int count = 0;
        int value = 0;

        if(!carrello.containsKey(idSupplier)) return new InfoSupplier(0,0);

        for(Map.Entry<Integer, Integer> e : carrello.get(idSupplier).entrySet()){
            count += e.getValue();
            try{
                value += productDAO.getPriceForProductFromSupplier(e.getKey(), idSupplier) * e.getValue();
            }catch (SQLException ex){
                throw new RuntimeException(ex);
            }
        }


        return new InfoSupplier(count,value);

    }

    private Map<Integer, Map<Integer, Integer>> getRealCart(){
        Map<Integer, Map<Integer, Integer>> carrello = (Map<Integer, Map<Integer, Integer>>)session.getAttribute("cart");
        if(carrello == null){
            carrello = new HashMap<>();
            session.setAttribute("cart",carrello);
        }

        return carrello;
    }

    public void removeProductOfSupplier(int idSupplier) {
        Map<Integer, Map<Integer,Integer>> carrello = getRealCart();
        carrello.remove(idSupplier);
    }

    public Map<Integer, Map<Integer, Integer>> getCart(){
        return Collections.unmodifiableMap(getRealCart());
    }
}
