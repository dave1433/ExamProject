package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Role;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import dk.easv.blsgn.intgrpbelsign.dal.exceptions.UsersException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {

    DatabaseConnection conn = new DatabaseConnection();
    @Override
    public ObservableList<User> getAllUsers() throws UsersException {
        ObservableList<User> users = FXCollections.observableArrayList();
        String sql = "SELECT * FROM [User] ORDER BY usage_count DESC";

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
                user.setUsage_count(rs.getInt("usage_count")); // ✅ Include usage count
                users.add(user);
            }
        } catch (Exception e) {
            throw new UsersException("Users could not be found. " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean addUser(User user) throws UsersException {
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
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new UsersException("User could not be added. " + e.getMessage());
        }
        return false;
    }
    @Override    public boolean editUser(User user) throws UsersException {
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
            if (rowsAffected > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new UsersException("User could not be edited. " + e.getMessage());
        }
        return false;
    }
    @Override    public boolean doesUserNameExist(String username) throws UsersException {
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


        } catch (SQLException e) {
            throw new UsersException(e);
        }
    }
    @Override    public User getUserByUsername(String username) throws UsersException {
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
            throw new UsersException(e);
        }
        return null;
    }
    @Override
    public List<Role> getAllRoles() throws UsersException {
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
            throw new UsersException("Roles could not be found. " + e.getMessage());
        }

        return roles;
    }

    @Override
    public byte[] getSignatureByUserId(int userId) throws UsersException {
        String sql = "SELECT signature FROM [User] WHERE user_id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("signature");
            }
        } catch (SQLException e) {
            throw new UsersException("Signature could not be found. " + e.getMessage());
        }
        return null;
    }

    @Override
    public void incrementUsageCount(int userId) throws UsersException {
        String sql = "UPDATE [User] SET usage_count = usage_count + 1 WHERE user_id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new UsersException("Usage count could not be incremented. " + e.getMessage());
        }
    }
}