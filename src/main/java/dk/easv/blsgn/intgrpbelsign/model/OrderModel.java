package dk.easv.blsgn.intgrpbelsign.model;

import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderModel {

    private final OrderManager orderManager;
    private ObservableList<Order> allOrders = FXCollections.observableArrayList();


    public OrderModel(OrderManager orderManager) {
        this.orderManager = new OrderManager();
        loadAllOrders();

    }

    public void loadAllOrders() {
        allOrders.setAll(orderManager.getAllOrders());
    }

    public ObservableList<Order> getAllOrders() {
        return  allOrders;
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

    public void updateImageStatus(int imageId, String status) {
        orderManager.updateImageStatus(imageId, status);
    }


    private final Map<Integer, String> tempStatuses = new HashMap<>();

    public void setTempStatus(int imageId, String status) {
        tempStatuses.put(imageId, status);
    }

    public String getTempStatus(int imageId) {
        return tempStatuses.getOrDefault(imageId, null);
    }

    public boolean hasTempStatus(int imageId) {
        return tempStatuses.containsKey(imageId);
    }

    public void clearTempStatusesForItem(int orderId, int itemId) {
        List<ImageWithMeta> images = getAllImagesWithStatus(orderId, itemId);
        for (ImageWithMeta img : images) {
            tempStatuses.remove(img.getId());
        }
    }
}