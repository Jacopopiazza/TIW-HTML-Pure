package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.Order;
import it.polimi.tiw.tiw_html_pure.Bean.OrderDetail;
import it.polimi.tiw.tiw_html_pure.Bean.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    private final Connection connection;

    public OrderDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Order> getOrdersForUser(String emailUtente) throws SQLException{

        String query = "SELECT * FROM ordine WHERE EmailUtente=? ORDER BY DataSpedizione DESC";
        PreparedStatement stmt = connection.prepareStatement(query);

        stmt.setString(1,emailUtente);

        ResultSet rs = stmt.executeQuery();

        if( !rs.isBeforeFirst() ) return new ArrayList<>();

        List<Order> ordini = new ArrayList<>();

        PreparedStatement stmt2 = connection.prepareStatement("SELECT * FROM dettaglioordine WHERE CodiceOrdine=?");

        while(rs.next()){
            stmt2.setInt(1,rs.getInt("Codice"));
            ResultSet rs2 = stmt2.executeQuery();

            if( !rs2.isBeforeFirst()) throw new SQLException("Inconsistent order");

            Order order;
            List<OrderDetail> orderDetails = new ArrayList<>();

            while(rs2.next()){
                //Here in rs there are all the products of order in rs
                OrderDetail orderDetail = new OrderDetail(rs.getInt("Codice"), rs2.getInt("CodiceProdotto"), rs2.getInt("PrezzoUnitario"), rs2.getInt("Quantita"));
                orderDetails.add(orderDetail);
            }
            Date date = rs.getDate("DataSpedizione");
            if(rs.wasNull()){
                date = null;
            }
            order = new Order(rs.getInt("Codice"), rs.getInt("CodiceFornitore"), rs.getInt("TotaleOrdine"), rs.getInt("SpeseSpedizione"),
                    rs.getString("Via"), rs.getString("Civico"), rs.getString("Citta"), rs.getString("Provincia"),
                    rs.getString("CAP"),rs.getString("Stato"), rs.getString("EmailUtente"), date, orderDetails);
            ordini.add(order);
        }
        return ordini;
    }

    public void createOrder(User user,int idSupplier, int speseSpedizione, int totaleOrdine, Map<Integer, Integer> prodottiOrdine) throws SQLException {

        connection.setAutoCommit(false);

        try {
            String query = "INSERT INTO ordine (CodiceFornitore, SpeseSpedizione, Via, Civico, CAP, Citta, Stato, Provincia, EmailUtente, TotaleOrdine) VALUES (?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt1 = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt1.setInt(1, idSupplier);
            stmt1.setInt(2, speseSpedizione);
            stmt1.setString(3, user.via());
            stmt1.setString(4, user.civico());
            stmt1.setString(5, user.CAP());
            stmt1.setString(6, user.citta());
            stmt1.setString(7, user.stato());
            stmt1.setString(8, user.provincia());
            stmt1.setString(9, user.email());
            stmt1.setInt(10, totaleOrdine);

            int affectedRows1 = stmt1.executeUpdate();
            if (affectedRows1 == 0) {
                throw new SQLException("Creazione dell'ordine fallita, nessuna riga inserita.");
            }

            //Ottengo l'id dell'ordine generato
            ResultSet rs = stmt1.getGeneratedKeys();

            int id_ordine = -1;
            if (rs.next()) {
                id_ordine = rs.getInt(1);
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
                stmt2.setInt(4, new ProductDAO(connection).getPriceForProductFromSupplier(e.getKey(),idSupplier));


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
