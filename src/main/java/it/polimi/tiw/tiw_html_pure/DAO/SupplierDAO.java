package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.DeliveryCost;
import it.polimi.tiw.tiw_html_pure.Bean.Product;
import it.polimi.tiw.tiw_html_pure.Bean.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierDAO {

    private final Connection connection;

    public SupplierDAO(Connection connection) {
        this.connection = connection;
    }

    public Supplier getSupplier(int codiceFornitore) throws SQLException {
        String query = "SELECT * FROM fornitore WHERE Codice=?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1,codiceFornitore);
        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()) return null;
        resultSet.next();

        Integer sogliaSpedizione = resultSet.getInt("SogliaSpedizioneGratuita");
        if(resultSet.wasNull()){
            sogliaSpedizione = null;
        }

        List<DeliveryCost> deliveryCostList = this.getDeliveryCostsForSupplier(resultSet.getInt("Codice"));
        return new Supplier(resultSet.getInt("Codice"),
                resultSet.getString("Nome"),
                resultSet.getDouble("Valutazione"),
                sogliaSpedizione,
                deliveryCostList);


    }
    public Map<Supplier, Double> getSuppliersAndPricesForProduct(int codiceProdotto) throws SQLException {

        String query = "SELECT F.*, Prezzo FROM prodottodafornitore pdf INNER JOIN fornitore F on pdf.CodiceFornitore=F.Codice WHERE CodiceProdotto=?";
        Map<Supplier, Double> suppliers = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, codiceProdotto);

        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()){
            return suppliers;
        }

        while (resultSet.next()) {

            Integer sogliaSpedizione = resultSet.getInt("SogliaSpedizioneGratuita");
            if(resultSet.wasNull()){
                sogliaSpedizione = null;
            }

            List<DeliveryCost> deliveryCostList = this.getDeliveryCostsForSupplier(resultSet.getInt("Codice"));
            Supplier supplier = new Supplier(resultSet.getInt("Codice"),
                    resultSet.getString("Nome"),
                    resultSet.getDouble("Valutazione"),
                    sogliaSpedizione,
                    deliveryCostList);

            suppliers.put(supplier, resultSet.getInt("Prezzo")/100.00);
        }

        return suppliers;

    }

    protected List<DeliveryCost> getDeliveryCostsForSupplier(int codiceFornitore) throws SQLException{
        String query = "SELECT * FROM fasciaspedizione WHERE CodiceFornitore=?";
        List<DeliveryCost> deliveryCosts = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, codiceFornitore);

        ResultSet resultSet = statement.executeQuery();

        if(!resultSet.isBeforeFirst()){
            return deliveryCosts;
        }

        while (resultSet.next()) {

            Integer numeroMassimoArticoli = resultSet.getInt("NumeroMassimoArticoli");
            if(resultSet.wasNull()){
                numeroMassimoArticoli = null;
            }

            DeliveryCost deliveryCost = new DeliveryCost(resultSet.getInt("idFasciaSpedizione"),
                    resultSet.getInt("CodiceFornitore"),
                    resultSet.getInt("NumeroMinimoArticoli"),
                    numeroMassimoArticoli,
                    resultSet.getInt("PrezzoSpedizione"));

            deliveryCosts.add(deliveryCost);
        }

        return deliveryCosts;
    }



}
