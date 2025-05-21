package dk.easv.blsgn.intgrpbelsign.model;

import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class OrderModel {

    private final OrderManager orderManager;
    private List<Order> allOrders;


    public OrderModel(OrderManager orderManager) {
        this.orderManager = new OrderManager();
        loadAllOrders();

    }

    public void loadAllOrders() {
        allOrders = orderManager.getAllOrders();
    }

    public List<Order> getAllOrders() {
        return allOrders;
    }

    public List<Order> filterOrders(String search) {
        if (search == null || search.isEmpty()) return allOrders;
        return allOrders.stream()
                .filter(order -> order.getOrderNumber().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    public ObservableList<String> getOrderNumbers(List<Order> orders) {
        return FXCollections.observableArrayList(
                orders.stream().map(Order::getOrderNumber).collect(Collectors.toList())
        );
    }

    /**
     * Find orders by their order number (for single selection).
     */
    public List<Order> findOrdersByNumber(String orderNumber) {
        if (orderNumber == null) return List.of();
        return allOrders.stream()
                .filter(order -> order.getOrderNumber().equals(orderNumber))
                .collect(Collectors.toList());
    }
    public List<ImageWithMeta> getAllImagesWithStatus(int orderId, int itemId) {
        return orderManager.getAllImagesWithStatus(orderId, itemId);
    }


    public void deleteImage(int id) {
        orderManager.deleteImage(id);
    }

    public void saveImage(int orderId, int id, byte[] imageBytes) throws SQLException {
        orderManager.saveImage(orderId, id, imageBytes);
    }
}