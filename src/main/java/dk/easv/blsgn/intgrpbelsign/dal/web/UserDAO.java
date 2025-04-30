package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    DatabaseConnection conn = new DatabaseConnection();

    public ObservableList<User> getAllUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM [User]";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("password_hash"),
                        rs.getInt("role_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone_number")
                );
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public User validateUser(String username, String passwordHash) {
        String sql = "SELECT * FROM [User] WHERE user_name = ? AND password_hash = ?";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("password_hash"),
                            rs.getInt("role_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone_number")

                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
