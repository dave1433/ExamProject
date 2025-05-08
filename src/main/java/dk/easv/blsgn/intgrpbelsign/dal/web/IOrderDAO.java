package dk.easv.blsgn.intgrpbelsign.dal.web;

import dk.easv.blsgn.intgrpbelsign.be.Order;

import java.sql.SQLException;
import java.util.List;

public interface IOrderDAO {
    void saveImage(int orderId, int itemId, byte[] imageBytes, int imageIndex) throws SQLException;

    List<byte[]> getImagesForItem(int orderId, int itemId);

    List<Order> getAllOrders();
}
