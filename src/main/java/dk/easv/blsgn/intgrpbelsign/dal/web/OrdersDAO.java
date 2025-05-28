package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.dal.exceptions.OrdersException;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdersDAO implements IOrderDAO {

    DatabaseConnection conn = new DatabaseConnection();

    @Override
    public List<Order> getAllOrders() throws OrdersException {
        List<Order> orders = new ArrayList<>();

        String orderSql = "SELECT id, order_number FROM orders";
        String itemSql = """
        SELECT items.id, items.item_name, order_item_image.order_id, order_item_image.isSubmitted
        FROM order_item_image
        JOIN items ON order_item_image.item_id = items.id
        WHERE order_item_image.order_id = ?
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
            throw new OrdersException("Orders could not be found" + e.getMessage());
        }

        return orders;
    }

    @Override
    public void saveImage(int orderId, int itemId, byte[] imageBytes, String viewType) throws OrdersException {
        String getLinkIdSql = "SELECT id FROM order_item_image WHERE order_id = ? AND item_id = ?";
        String insertSql = "INSERT INTO item_images (fk_order_item_image_id, image_data, status, viewType) VALUES (?, ?, 'pending', ?)";

        try (Connection c = conn.getConnection();
             PreparedStatement getLinkStmt = c.prepareStatement(getLinkIdSql)) {

            getLinkStmt.setInt(1, orderId);
            getLinkStmt.setInt(2, itemId);
            ResultSet rs = getLinkStmt.executeQuery();

            if (rs.next()) {
                int linkId = rs.getInt("id");

                try (PreparedStatement insertStmt = c.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, linkId);
                    insertStmt.setBytes(2, imageBytes);
                    insertStmt.setString(3, viewType);
                    insertStmt.executeUpdate();
                }
            } else {
                throw new OrdersException("No order");
            }
        }
        catch (SQLException e) {
            throw new OrdersException("Could not save image for order " + orderId + " and item " + itemId + " " + e.getMessage());
        }
    }


    @Override
    public void updateImageStatus(int imageId, String status) throws OrdersException {
        String sql = "UPDATE item_images SET status = ? WHERE id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, imageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrdersException("Could not update image status for image =" + imageId + " " + e.getMessage());
        }
    }

    @Override
    public List<ImageWithMeta> getAllImagesWithStatus(int orderId, int itemId) throws OrdersException {
        List<ImageWithMeta> imageList = new ArrayList<>();
        String sql = "SELECT  item_images.id, item_images.image_data, item_images.status, item_images.viewType " +
                "FROM item_images " +
                "JOIN order_item_image on " +
                "item_images.fk_order_item_image_id=order_item_image.id " +
                "WHERE order_item_image.order_id = ? " +
                "AND order_item_image.item_id = ? " +
                "ORDER BY item_images.id ";

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
            throw new OrdersException("Images could not be found for order " + orderId + " and item=" + itemId + " " + e.getMessage());
        }

        return imageList;
    }

    @Override
    public void deleteImage(int imageId) throws OrdersException {
        String sql = "DELETE FROM item_images WHERE id = ?";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, imageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrdersException("Could not delete image with id=" + imageId + " " + e.getMessage());
        }
    }

    @Override
    public void markItemAsSubmitted(int orderId, int itemId) throws OrdersException {
        String sql = "UPDATE order_item_image SET isSubmitted = 1 WHERE order_id = ? AND item_id = ?";
        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OrdersException("Could not mark item as submitted " + e.getMessage());
        }
    }

    @Override
    public void markItemAsUnsubmitted(int orderId, int itemId) throws OrdersException {
        String sql = "UPDATE order_item_image SET isSubmitted = 0 WHERE order_id = ? AND item_id = ?";

        try (Connection c = conn.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new OrdersException(e);
        }
    }
}