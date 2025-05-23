package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;

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
        SELECT i.id, i.item_name, oii.order_id, i.isSubmitted
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
                            item.setSubmitted(itemRs.getBoolean("isSubmitted"));
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
    public void saveImage(int orderId, int itemId, byte[] imageBytes, String viewType) throws SQLException {
        String sqlInsert = "INSERT INTO item_images (order_id, item_id, image_data, status, viewType) VALUES (?, ?, ?, 'pending', ?)";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sqlInsert)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            stmt.setBytes(3, imageBytes);
            stmt.setString(4, viewType); // NEW: add viewType here
            stmt.executeUpdate();
        }
    }



    @Override
    public List<byte[]> getImagesForItem(int orderId, int itemId) {
        List<byte[]> images = new ArrayList<>();
        String sql = "SELECT item_images.image_data " +
                     "FROM item_images " +
                     "JOIN order_item_image ON item_images.fk_order_item_image_id = order_item_image.id " +
                     "WHERE order_item_image.order_id = ? " +
                     "AND order_item_image.item_id = ? " +
                     "AND item_images.status = 'approved' " +
                     "ORDER BY item_images.id";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] img = rs.getBytes("image_data");
                if (img != null) {
                    images.add(img);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return images;
    }

    @Override
    public void updateImageStatus(int imageId, String status) {
        String sql = "UPDATE item_images SET status = ? WHERE id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, imageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ImageWithMeta> getAllImagesWithStatus(int orderId, int itemId) {
        List<ImageWithMeta> imageList = new ArrayList<>();
        String sql = "SELECT  item_images.id, item_images.image_data, item_images.status, item_images.viewType " +
                "FROM item_images " +
                "JOIN order_item_image on " +
                "item_images.fk_order_item_image_id=order_item_image.id " +
                "WHERE order_item_image.order_id = ? " +
                "AND order_item_image.item_id = ? " +
                "ORDER BY item_images.id ";
       /* String sql = "SELECT item_images.id, item_images.image_data, item_images.status, item_images.viewType " +
                "FROM item_images " +
                "JOIN order_item_image ON item_images.fk_order_item_image_id = order_item_image.id " +
                "WHERE order_item_image.order_id = ? " +
                "AND order_item_image.item_id = ? " +
                "ORDER BY item_images.id";*/



        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                byte[] img = rs.getBytes("image_data");
                String status = rs.getString("status");
                String viewType = rs.getString("viewType");

                if (img != null) {
                    imageList.add(new ImageWithMeta(id, img, status != null ? status : "pending", viewType));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return imageList;
    }

    @Override
    public void deleteImage(int imageId) {
        String sql = "DELETE FROM item_images WHERE id = ?";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, imageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void markItemAsSubmitted(int itemId) {
        String sql = "UPDATE items SET isSubmitted = 1 WHERE id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}