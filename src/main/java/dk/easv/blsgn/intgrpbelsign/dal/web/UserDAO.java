package dk.easv.blsgn.intgrpbelsign.dal.web;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public boolean addUser(User user) {
        String sql = "INSERT INTO [User] (user_name, password_hash, role_id, first_name, last_name, email, phone_number)" + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = conn.getConnection()) {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, user.getUser_name());
            stmt.setString(2, user.getPassword_hash());
            stmt.setInt(3, user.getRole_id());
            stmt.setString(4, user.getFirst_name());
            stmt.setString(5, user.getLast_name());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getPhone_number());

            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
           e.printStackTrace();
        }
        return false;
    }

    public boolean editUser(User user) {
        String sql = "UPDATE [User] SET user_name = ?, password_hash = ?, role_id = ?, first_name = ?, last_name = ?, email = ?, phone_number = ? WHERE user_id = ?";
        try (Connection c = conn.getConnection()) {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, user.getUser_name());
            stmt.setString(2, user.getPassword_hash());
            stmt.setInt(3, user.getRole_id());
            stmt.setString(4, user.getFirst_name());
            stmt.setString(5, user.getLast_name());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getPhone_number());
            stmt.setInt(8, user.getUser_id());

            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doesUserNameExist(String username) {
        String sql = "SELECT COUNT(*) FROM [User] WHERE user_name = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
            return false;


        } catch (SQLServerException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM [User] WHERE user_name = ?";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);

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

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_id, role_name FROM Role";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("role_id");
                String name = rs.getString("role_name");
                roles.add(new Role(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roles;
    }
}
