package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.User;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProductDAO {

    private final Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Product> getMenuProductsForUser(String email)throws SQLException {

        String query = "SELECT P.* , Timestamp FROM visualizzazioni v1 INNER JOIN prodotto P on P.Codice=v1.CodiceProdotto WHERE EmailUtente=? and Timestamp = (SELECT MAX(Timestamp) FROM visualizzazioni v2 WHERE v2.EmailUtente=v1.EmailUtente AND v2.CodiceProdotto=v1.CodiceProdotto) ORDER BY Timestamp DESC LIMIT 5";
        List<Product> lasts = new ArrayList<>();

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        ResultSet resultSet = statement.executeQuery();

        if(resultSet.isBeforeFirst()) {


            while (resultSet.next()) {
                Product product = new Product(resultSet.getInt("Codice"),
                        resultSet.getString("Nome"),
                        resultSet.getString("Descrizione"),
                        resultSet.getString("Foto"),
                        resultSet.getString("Categoria"));
                lasts.add(product);
            }

        }

        if(lasts.size() < 5){

            Queue<Product> randomProducts;
            randomProducts = getFiveRandomProducts();


            while(!randomProducts.isEmpty() && lasts.size() < 5){
                Product p = randomProducts.poll();
                if(!lasts.contains(p)){
                    lasts.add(p);
                }
            }
        }

        return lasts;
    }

    public String getFotoPathFromCodiceProdotto(int codiceProdotto) throws SQLException{
        String query = "SELECT Foto FROM prodotto WHERE Codice = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, codiceProdotto);
        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()){
            return null;
        }

        resultSet.next();
        return resultSet.getString("Foto");
    }

    public Queue<Product> getFiveRandomProducts() throws SQLException{
        String query = "SELECT * FROM prodotto ORDER BY RAND() LIMIT 5";
        Queue<Product> lasts = new LinkedList<>();
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()){
            return lasts;
        }

        while (resultSet.next()) {
            Product product = new Product(resultSet.getInt("Codice"),
                    resultSet.getString("Nome"),
                    resultSet.getString("Descrizione"),
                    resultSet.getString("Foto"),
                    resultSet.getString("Categoria"));
            lasts.add(product);
        }




        return lasts;
    }

    public Map<Product, Double> getProductsFromQueryString(String queryString) throws SQLException{
        String query = "SELECT P.*, Min(Prezzo) AS PrezzoMinimo FROM db_tiw.prodotto P INNER JOIN db_tiw.prodottodafornitore PDF on P.Codice=PDF.CodiceProdotto WHERE P.Nome LIKE ? OR P.Descrizione=? GROUP BY CodiceProdotto ORDER BY PrezzoMinimo;";
        Map<Product, Double> prods = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setString(1, "%" + queryString + "%");
        statement.setString(2, "%" + queryString + "%");

        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()){
            return prods;
        }

        while (resultSet.next()) {
            Product product = new Product(resultSet.getInt("Codice"),
                    resultSet.getString("Nome"),
                    resultSet.getString("Descrizione"),
                    resultSet.getString("Foto"),
                    resultSet.getString("Categoria"));

            prods.put(product, resultSet.getInt("PrezzoMinimo")/100.00);
        }

        return prods;
    }

    public void prodottoVisualizzato(User user, int codiceProdotto) throws SQLException{
        String query = "INSERT into visualizzazioni (EmailUtente, CodiceProdotto)   VALUES(?, ?)";
        PreparedStatement pstatement = connection.prepareStatement(query);
        pstatement.setString(1, user.email());
        pstatement.setInt(2, codiceProdotto);
        pstatement.executeUpdate();
    }
}
