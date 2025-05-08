// OrderManager.java
package dk.easv.blsgn.intgrpbelsign.bll;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.dal.web.IOrderDAO;
import dk.easv.blsgn.intgrpbelsign.dal.web.OrdersDAO;

import java.sql.SQLException;
import java.util.List;

public class OrderManager {
    private final IOrderDAO iOrderDAO = new OrdersDAO();

    public List<Order> getAllOrders() {
        return iOrderDAO.getAllOrders();
    }

    public void saveImage(int orderId, int itemId, byte[] imageBytes, int imageIndex) throws SQLException {
        iOrderDAO.saveImage(orderId, itemId, imageBytes, imageIndex);
    }
     public List<byte[]> getImagesForItem(int orderId, int itemId){
        return iOrderDAO.getImagesForItem(orderId, itemId);
     }
}