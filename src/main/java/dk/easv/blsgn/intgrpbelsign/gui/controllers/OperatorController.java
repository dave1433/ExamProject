// OrderViewController.java
package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.List;
import java.util.stream.Collectors;

public class OperatorController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    private final OrderManager orderManager = new OrderManager();

    @FXML
    public void initialize()  {
        // Optionally load all orders on start
        displayOrd(orderManager.getAllOrders());
    }


    @FXML
    private void onSearch() {
        String input = searchField.getText().trim();

        if (!input.isEmpty()) {
            try {
                int searchedItemNumber = Integer.parseInt(input);
                List<Order> matchingOrders = orderManager.getAllOrders()
                        .stream()
                        .filter(o -> o.getOrderNumber() == searchedItemNumber)
                        .collect(Collectors.toList());

                displayOrders(matchingOrders);
            } catch (NumberFormatException e) {
                // handle invalid input (non-integer)
                flowPane.getChildren().clear();
                flowPane.getChildren().add(new Label("Please enter a valid number."));
            }
        }
    }

    private void displayOrders(List<Order> orders) {
        flowPane.getChildren().clear();

        for (Order order : orders) {
            VBox vbox = new VBox();
            vbox.setSpacing(10);
            vbox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
            vbox.setPrefWidth(700);
            vbox.setPrefHeight(150);

            // Create the item number label
            Label itemNumberLabel = new Label("Item: " + order.getItemNumber());

            // Load the icon image (adjust path based on your resources structure)
            Image icon = new Image(getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/camera.png")); // <-- replace with your path
            javafx.scene.image.ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(100);
            iconView.setFitHeight(100);

            // Create a button with only the icon
            Button detailsButton = new Button();
            detailsButton.setGraphic(iconView);
            detailsButton.setPrefSize(150, 150);
            detailsButton.setMinSize(150, 150);
            detailsButton.setMaxSize(150, 150);
            detailsButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            // Optional: add a tooltip
            Tooltip.install(detailsButton, new Tooltip("View details"));

            // Place the label and button side-by-side in a FlowPane
            FlowPane innerFlowPane = new FlowPane();
            innerFlowPane.setHgap(10);
            innerFlowPane.getChildren().addAll(itemNumberLabel, detailsButton);

            // Add everything to the outer VBox
            vbox.getChildren().add(innerFlowPane);

            // Add the VBox to the main flowPane
            flowPane.getChildren().add(vbox);
        }
    }


    private void displayOrd(List<Order> orders) {
        ObservableList<String> orderNumbers = FXCollections.observableArrayList();

        for (Order order : orders) {
            // Add each order number to the ListView
            orderNumbers.add("Order: " + order.getOrderNumber());
        }

        // Set the list of order numbers to the ListView
        listView.setItems(orderNumbers);
    }

}
