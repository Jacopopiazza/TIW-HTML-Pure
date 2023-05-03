package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.User;

import java.sql.*;
import java.util.Map;

public class OrderDAO {

    private final Connection connection;

    public OrderDAO(Connection connection) {
        this.connection = connection;
    }

    public void createOrder(User user,int codiceFornitore, int speseSpedizione, int totaleOrdine, Map<Integer, Integer> prodottiOrdine) throws SQLException {

        connection.setAutoCommit(false);

        try {
            String query = "INSERT INTO ordine (CodiceFornitore, SpeseSpedizione, Via, Civico, CAP, Citta, Stato, Provincia, EmailUtente, TotaleOrdine) VALUES (?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt1 = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt1.setInt(1, codiceFornitore);
            stmt1.setInt(2, speseSpedizione);
            stmt1.setString(3, user.via());
            stmt1.setString(4, user.civico());
            stmt1.setString(5, user.CAP());
            stmt1.setString(6, user.citta());
            stmt1.setString(7, user.stato());
            stmt1.setString(8, user.provincia());
            stmt1.setString(9, user.email());
            stmt1.setInt(10, totaleOrdine);

            stmt1.executeUpdate();
            int affectedRows1 = stmt1.executeUpdate();
            if (affectedRows1 == 0) {
                throw new SQLException("Creazione dell'ordine fallita, nessuna riga inserita.");
            }

            //Ottengo l'id dell'ordine generato
            ResultSet rs = stmt1.getGeneratedKeys();
            int id_ordine = -1;
            if (rs.next()) {
                id_ordine = rs.getInt("Codice");
            } else {
                throw new SQLException("Creazione dell'ordine fallita, impossibile ottenere l'ID generato.");
            }

            //Creo righe dell'ordine
            query = "INSERT INTO dettaglioordine (CodiceOrdine, CodiceProdotto, Quantita, PrezzoUnitario) VALUES (?,?,?,?);";
            PreparedStatement stmt2 = connection.prepareStatement(query);
            for(Map.Entry<Integer,Integer> e : prodottiOrdine.entrySet()){

                stmt2.setInt(1, id_ordine);
                stmt2.setInt(2, e.getKey());
                stmt2.setInt(3, e.getValue());
                stmt2.setInt(4, new ProductDAO(connection).getPriceForProductFromSupplier(e.getKey(),codiceFornitore));

                int affectedRows2 = stmt2.executeUpdate();
                if (affectedRows2 == 0) {
                    throw new SQLException("Creazione del dettaglio dell'ordine fallita, nessuna riga inserita.");
                }

            }

            // Se tutto va bene, commit della transazione
            connection.commit();

        }catch (SQLException ex){
            if(connection != null) connection.rollback();
            throw new SQLException(ex);
        }
        finally {
            connection.setAutoCommit(true);
        }

    }
}
