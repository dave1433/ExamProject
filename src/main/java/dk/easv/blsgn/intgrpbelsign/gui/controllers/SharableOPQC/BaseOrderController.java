package dk.easv.blsgn.intgrpbelsign.gui.controllers.SharableOPQC;

import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;

import java.util.List;

public abstract class BaseOrderController {
    /*protected final OrderModel orderModel = new OrderModel(new OrderManager());
    protected List<Order> allOrders;

    protected boolean hasPendingImages(Order order) {
        for (Item item : order.getItems()) {
            List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
            if (images.stream().anyMatch(img -> "pending".equalsIgnoreCase(img.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasRejectedImages(Order order) {
        for (Item item : order.getItems()) {
            List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
            if (images.stream().anyMatch(img -> "rejected".equalsIgnoreCase(img.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    protected String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> "-fx-text-fill: green; -fx-font-size: 15px";
            case "rejected" -> "-fx-text-fill: red; -fx-font-size: 15px";
            default -> "-fx-text-fill: orange; -fx-font-size: 15px";
        };
    }

    protected void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void styleOrderLabel(Labeled labeled, boolean highlight) {
        if (highlight) {
            labeled.setStyle("-fx-border-color: #f19352; -fx-border-width: 2px; -fx-border-radius: 3px;");
        } else {
            labeled.setStyle("-fx-background-insets: 0 0 3px 0;");
        }
    }*/
}
