// OrderManager.java
package dk.easv.blsgn.intgrpbelsign.bll;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.dal.web.OrdersDAO;

import java.util.List;

public class OrderManager {
    private final OrdersDAO orderDAO = new OrdersDAO();

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }
}