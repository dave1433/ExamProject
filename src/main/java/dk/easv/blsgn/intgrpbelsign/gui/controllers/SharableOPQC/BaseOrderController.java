package dk.easv.blsgn.intgrpbelsign.gui.controllers.SharableOPQC;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.gui.model.OrderModel;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class BaseOrderController {

    protected List<Order> allOrders; // Shared orders list accessible by subclasses

    /**
     * Sets up order list and interaction logic
     */
    protected void initializeOrderList(
            TextField searchField,
            ListView<String> listView,
            FlowPane flowPane,
            OrderModel orderModel,
            Predicate<Order> highlightCondition,
            Function<String, List<Order>> filterFunction,
            Consumer<List<Order>> onOrderSelected
    ) {

        this.allOrders = orderModel.getAllOrders();

        System.out.println("initializeOrderList: " + allOrders.size() + " orders loaded.");

        // Load all orders initially
        listView.setItems(orderModel.getOrderNumbers(allOrders));

        // Handle text search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Order> filtered = filterFunction.apply(newVal);
            listView.setItems(orderModel.getOrderNumbers(allOrders));
            if (flowPane != null) flowPane.getChildren().clear();
        });

        // Handle order selection
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedOrderNumber) -> {
            if (selectedOrderNumber != null) {
                List<Order> selected = allOrders.stream()
                        .filter(order -> order.getOrderNumber().equals(selectedOrderNumber))
                        .toList();
                onOrderSelected.accept(selected);
            }
        });

        // Apply styling logic to list cells
        applyOrderListStyling(listView, allOrders, highlightCondition);

        // Optional: clear content on startup
        if (flowPane != null) {
            flowPane.getChildren().clear();
        }
    }

    /**
     * Highlights orders with a custom rule (e.g. rejected/pending images)
     */
    protected void applyOrderListStyling(
            ListView<String> listView,
            List<Order> allOrders,
            Predicate<Order> highlightCondition
    ) {
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setStyle("");

                if (empty || item == null) return;

                setText(item);

                Order order = allOrders.stream()
                        .filter(o -> o.getOrderNumber().equals(item))
                        .findFirst().orElse(null);

                if (order != null && highlightCondition.test(order)) {
                    setStyle("-fx-border-color: #f19352; -fx-border-width: 2px; -fx-border-radius: 3px;");
                } else {
                    setStyle("-fx-background-insets: 0 0 3px 0;");
                }
            }
        });
    }
}
