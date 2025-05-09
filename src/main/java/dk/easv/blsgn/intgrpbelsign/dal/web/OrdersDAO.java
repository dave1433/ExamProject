package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersDAO implements IOrderDAO {

    DatabaseConnection conn = new DatabaseConnection();

    @Override
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();

        String orderSql = "SELECT id, order_number FROM orders";
        String itemSql = """
            SELECT i.id, i.item_name, oii.order_id
            FROM order_item_image oii
            JOIN items i ON oii.item_id = i.id
            WHERE oii.order_id = ?
        """;

        try (Connection c = conn.getConnection();
             PreparedStatement orderStmt = c.prepareStatement(orderSql);
             ResultSet orderRs = orderStmt.executeQuery()) {

            while (orderRs.next()) {
                int orderId = orderRs.getInt("id");
                String orderNumber = orderRs.getString("order_number");

                List<Item> itemList = new ArrayList<>();

                try (PreparedStatement itemStmt = c.prepareStatement(itemSql)) {
                    itemStmt.setInt(1, orderId);
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        while (itemRs.next()) {
                            Item item = new Item(
                                    itemRs.getInt("id"),
                                    itemRs.getString("item_name"),
                                    itemRs.getInt("order_id")
                            );
                            itemList.add(item);
                        }
                    }
                }

                Order order = new Order(orderId, orderNumber, itemList);
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }
    @Override
    public void saveImage(int orderId, int itemId, byte[] imageBytes, int imageIndex) throws SQLException {
        String columnName = "image" + imageIndex;
        String sqlCheck = "SELECT * FROM order_item_image WHERE order_id = ? AND item_id = ?";
        String sqlInsert = "INSERT INTO order_item_image (order_id, item_id, " + columnName + ") VALUES (?, ?, ?)";
        String sqlUpdate = "UPDATE order_item_image SET " + columnName + " = ? WHERE order_id = ? AND item_id = ?";

        try (Connection c = conn.getConnection()) {
            PreparedStatement checkStmt = c.prepareStatement(sqlCheck);
            checkStmt.setInt(1, orderId);
            checkStmt.setInt(2, itemId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                PreparedStatement updateStmt = c.prepareStatement(sqlUpdate);
                updateStmt.setBytes(1, imageBytes);
                updateStmt.setInt(2, orderId);
                updateStmt.setInt(3, itemId);
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = c.prepareStatement(sqlInsert);
                insertStmt.setInt(1, orderId);
                insertStmt.setInt(2, itemId);
                insertStmt.setBytes(3, imageBytes);
                insertStmt.executeUpdate();
            }
        }
    }
    @Override
    public List<byte[]> getImagesForItem(int orderId, int itemId) {
        List<byte[]> images = new ArrayList<>();
        String sql = "SELECT image1, image2, image3, image4, image5 FROM order_item_image WHERE order_id = ? AND item_id = ?";

        try (Connection c = conn.getConnection()) {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                for (int i = 1; i <= 5; i++) {
                    byte[] img = rs.getBytes("image" + i);
                    if (img != null) {
                        images.add(img);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return images;
    }
}
