package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.dal.connection.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImageDAO {
    private final DatabaseConnection conn = new DatabaseConnection();

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
