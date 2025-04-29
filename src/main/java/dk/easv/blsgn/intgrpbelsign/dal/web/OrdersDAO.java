package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

    public class OrdersDAO {

        DatabaseConnection conn = new DatabaseConnection();

        public List<Order> getAllOrders() {
            List<Order> orders = new ArrayList<>();
            String sql = "SELECT item_id, order_number, image FROM Orders";


            try (Connection c = conn.getConnection();
                 PreparedStatement stmt = c.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Order order = new Order(
                            rs.getInt("order_number"), // first: order number
                            rs.getInt("item_id"),      // second: item number
                            rs.getString("image"));
                    orders.add(order);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return orders;
        }
    }
