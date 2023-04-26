package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ProductDAO {

    private final Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    public Queue<Product> getLastViewedProductsForUser(String email)throws SQLException {

        String query = "SELECT p.* FROM visualizzazioni v INNER JOIN prodotto p ON v.CodiceProdotto = p.Codice WHERE EmailUtente = ? ORDER BY Timestamp DESC";
        Queue<Product> lasts = new LinkedList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
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


        }

        return lasts;
    }

    public Queue<Product> getFiveRandomProducts() throws SQLException{
        String query = "SELECT * FROM prodotto ORDER BY RAND()";
        Queue<Product> lasts = new LinkedList<>();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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


        }

        return lasts;
    }
}
