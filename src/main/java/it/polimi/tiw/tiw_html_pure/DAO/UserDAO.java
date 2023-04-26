package it.polimi.tiw.tiw_html_pure.DAO;

import it.polimi.tiw.tiw_html_pure.Bean.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }


    public boolean doesEmailExist(String email) throws SQLException {
        String query = "SELECT Email FROM utente WHERE Email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.isBeforeFirst();
            }
        }
    }

    public User checkCredentials(String email, String password) throws SQLException {
        String query = "SELECT * FROM utente WHERE Email = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);


            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.isBeforeFirst())
                    return null;
                resultSet.next();
                return new User(resultSet.getString("Email"),
                        resultSet.getString("Nome"),
                        resultSet.getString("Cognome"),
                        resultSet.getString("Via"),
                        resultSet.getString("Civico"),
                        resultSet.getString("CAP"),
                        resultSet.getString("Citta"),
                        resultSet.getString("Stato"),
                        resultSet.getString("Provincia"));
            }
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}])|(([a-zA-Z\\-\\d]+\\.)+[a-zA-Z]{2,}))$")
                && email.length() <= 50;
    }



}
