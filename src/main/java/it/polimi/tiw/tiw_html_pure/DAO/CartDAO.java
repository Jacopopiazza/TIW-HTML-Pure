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

        Map<ProductBySupplier, Integer> carrello = (Map<ProductBySupplier, Integer>)session.getAttribute("cart");
        if(carrello == null){
            carrello = new HashMap<>();
            session.setAttribute("cart",carrello);
        }
        ProductBySupplier pbs = new ProductBySupplier(codiceProdotto,codiceFornitore);
        int old = 0;
        if(carrello.keySet().contains(pbs)){
            old = carrello.get(pbs);
        }

        carrello.put(pbs, old + quantita);


    }

    public InfoSupplier getInformationForSupplier(int codiceFornitore) {

        ProductDAO productDAO = new ProductDAO(connection);
        Map<ProductBySupplier, Integer> carrello = getRealCart();
        int count = 0;
        int value = 0;
        for (Map.Entry<ProductBySupplier, Integer> e : carrello.entrySet()){
            if(e.getKey().codiceFornitore() != codiceFornitore) continue;
            count += e.getValue();
            try{
                value += productDAO.getPriceForProductFromSupplier(e.getKey().codiceProdotto(), e.getKey().codiceFornitore()) * e.getValue();
            }catch (SQLException ex){
                System.out.println("Errore nel recupero dei prezzi... controlla");
                System.out.println("Codice Fornitore: " +e.getKey().codiceFornitore() + ", Codice Prodotto: " + e.getKey().codiceProdotto());
                System.out.println(ex.getMessage());
                throw new RuntimeException(ex);
            }

        }

        InfoSupplier info = new InfoSupplier(count,value);

        return info;
    }

    private Map<ProductBySupplier, Integer> getRealCart(){
        Map<ProductBySupplier, Integer> carrello = (Map<ProductBySupplier, Integer>)session.getAttribute("cart");
        if(carrello == null){
            carrello = new HashMap<>();
            session.setAttribute("cart",carrello);
        }

        return carrello;
    }

    public Map<ProductBySupplier, Integer> getCart(){
        return Collections.unmodifiableMap(getRealCart());
    }
}
