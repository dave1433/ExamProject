package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Order;

import java.sql.SQLException;
import java.util.List;

public interface IOrderDAO {
    /**
     * Saves an image to the database for a specific order and item.
     *
     * @param orderId     The ID of the order.
     * @param itemId      The ID of the item.
     * @param imageBytes  The byte array representing the image.
     * @param imageIndex  The index of the image in the list of images for that item.
     * @throws SQLException If there is an error while saving the image to the database.
     */
    void saveImage(int orderId, int itemId, byte[] imageBytes, int imageIndex) throws SQLException;

    List<byte[]> getImagesForItem(int orderId, int itemId);

    List<Order> getAllOrders();

}
