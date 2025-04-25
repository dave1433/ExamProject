package dk.easv.blsgn.intgrpbelsign.dal;

import dk.easv.blsgn.intgrpbelsign.be.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;




public class UserDAO {

    private static DBConnection connection = new DBConnection();

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_name FROM [User]";

        try (Connection conn = connection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(new User(rs.getString("user_name")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
