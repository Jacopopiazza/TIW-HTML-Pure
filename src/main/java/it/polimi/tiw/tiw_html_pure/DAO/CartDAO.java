package it.polimi.tiw.tiw_html_pure.DAO;

import com.mysql.cj.conf.ConnectionUrlParser;
import it.polimi.tiw.tiw_html_pure.Bean.InfoSupplier;
import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.ProductBySupplier;
import it.polimi.tiw.tiw_html_pure.Bean.Supplier;
import it.polimi.tiw.tiw_html_pure.Utilities.Pair;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CartDAO {

    private final HttpSession session;
    private final Connection connection;

    public CartDAO(HttpSession session, Connection connection) {
        this.session = session;
        this.connection = connection;
    }

    public void addProductToCart(int codiceProdotto, int codiceFornitore, int quantita){

        Map<Integer, Map<Integer, Integer>> carrello = getRealCart();

        int old = 0;

        if(!carrello.containsKey(codiceFornitore)){
            carrello.put(codiceFornitore, new HashMap<>());
        }

        if(carrello.get(codiceFornitore).containsKey(codiceProdotto)){
            old = carrello.get(codiceFornitore).get(codiceProdotto);
        }

        carrello.get(codiceFornitore).put(codiceProdotto, old + quantita);

    }

    public InfoSupplier getInformationForSupplier(int codiceFornitore) {

        ProductDAO productDAO = new ProductDAO(connection);
        Map<Integer, Map<Integer, Integer>> carrello = getRealCart();
        int count = 0;
        int value = 0;

        if(!carrello.containsKey(codiceFornitore)) return new InfoSupplier(0,0);

        for(Map.Entry<Integer, Integer> e : carrello.get(codiceFornitore).entrySet()){
            count += e.getValue();
            try{
                value += productDAO.getPriceForProductFromSupplier(e.getKey(), codiceFornitore) * e.getValue();
            }catch (SQLException ex){
                System.out.println("Errore nel recupero dei prezzi... controlla");
                System.out.println("Codice Fornitore: " + codiceFornitore + ", Codice Prodotto: " + e.getKey());
                System.out.println(ex.getMessage());
                throw new RuntimeException(ex);
            }
        }


        InfoSupplier info = new InfoSupplier(count,value);

        return info;
    }

    private Map<Integer, Map<Integer, Integer>> getRealCart(){
        Map<Integer, Map<Integer, Integer>> carrello = (Map<Integer, Map<Integer, Integer>>)session.getAttribute("cart");
        if(carrello == null){
            carrello = new HashMap<>();
            session.setAttribute("cart",carrello);
        }

        return carrello;
    }

    public Map<Integer, Map<Integer, Integer>> getCart(){
        return Collections.unmodifiableMap(getRealCart());
    }
}
