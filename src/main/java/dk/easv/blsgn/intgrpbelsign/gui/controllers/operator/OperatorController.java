package dk.easv.blsgn.intgrpbelsign.gui.controllers.operator;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import dk.easv.blsgn.intgrpbelsign.utils.ImageOverlayUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.util.List;

public class OperatorController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    private final OrderModel orderModel = new OrderModel(new OrderManager());
    private final ImageOverlayUtil imageOverlayUtil = new ImageOverlayUtil(new OrderManager());

    public OperatorController() {
        imageOverlayUtil.setRefreshCallback(this::refreshView);
    }

    private void refreshView() {
        String selectedOrderNumber = listView.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber != null) {
            List<Order> selected = orderModel.findOrdersByNumber(selectedOrderNumber);
            displayOrders(selected);
        }
    }

    @FXML
    public void initialize() {
        onSearchFilter();
    }

    @FXML
    private void onSearchFilter() {
        orderModel.loadAllOrders();
        displayOrd(orderModel.getAllOrders());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Order> filtered = orderModel.filterOrders(newVal);
            displayOrd(filtered);
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedOrderNumber) -> {
            if (selectedOrderNumber != null) {
                List<Order> selected = orderModel.findOrdersByNumber(selectedOrderNumber);
                displayOrders(selected);
            }
        });
    }

    private void displayOrd(List<Order> orders) {
        listView.setItems(orderModel.getOrderNumbers(orders));
    }

    private void displayOrders(List<Order> orders) {
        flowPane.getChildren().clear();

        for (Order order : orders) {
            VBox orderBox = createOrderBox(order);
            flowPane.getChildren().add(orderBox);
        }
    }

    private VBox createOrderBox(Order order) {
        VBox orderBox = new VBox(10);
        orderBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
        orderBox.setPrefWidth(700);

        Label orderLabel = new Label("Order: " + order.getOrderNumber());
        orderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox itemsContainer = new VBox(15);
        for (Item item : order.getItems()) {
            VBox itemBox = createItemBox(item, order);
            itemsContainer.getChildren().add(itemBox);
        }

        orderBox.getChildren().addAll(orderLabel, itemsContainer);
        return orderBox;
    }

    private VBox createItemBox(Item item, Order order) {
        VBox itemBox = new VBox(5);
        itemBox.setAlignment(Pos.TOP_LEFT);
        itemBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
        itemBox.setPrefWidth(650);

        Label itemNameLabel = new Label(item.getItemName());
        itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        VBox photoSections = createPhotoSections(item, order);

        Button addPhotoButton = createAddPhotoButton(item, order);

        itemBox.getChildren().addAll(itemNameLabel, photoSections, addPhotoButton);
        return itemBox;
    }

    private VBox createPhotoSections(Item item, Order order) {
        VBox photoSections = new VBox(10);
        FlowPane rejectedPane = new FlowPane(5, 5);
        FlowPane approvedPane = new FlowPane(5, 5);
        FlowPane pendingPane = new FlowPane(5, 5);

        List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
        String[] viewLabels = {"Front", "Back", "Left", "Right", "Top", "Bottom"};
        int imageCount = 0;

        for (ImageWithMeta meta : images) {
            Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(150);
            imgView.setFitHeight(150);
            imgView.setPreserveRatio(true);

            StackPane imageStack = imageOverlayUtil.createImageWithOverlay(
                    imgView,
                    img,
                    meta.getStatus(),
                    meta,
                    item,
                    order.getID(),
                    order.getOrderNumber(),
                    false
            );

            // Add the view label if it's one of the first 6 images
            if (imageCount < viewLabels.length) {
                VBox labeledImageStack = new VBox(5); // 5 px spacing between label and image
                Label viewLabel = new Label(viewLabels[imageCount]);
                viewLabel.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-padding: 2 5; " +
                        "-fx-border-color: black; -fx-border-radius: 3;");
                labeledImageStack.setAlignment(Pos.CENTER);
                labeledImageStack.getChildren().addAll(viewLabel, imageStack);
                imageStack = new StackPane(labeledImageStack);
            }

            switch (meta.getStatus().toLowerCase()) {
                case "rejected" -> rejectedPane.getChildren().add(imageStack);
                case "approved" -> approvedPane.getChildren().add(imageStack);
                default -> pendingPane.getChildren().add(imageStack);
            }

            imageCount++;
        }

        addLabeledSection(photoSections, rejectedPane, "❌ Rejected - Re-take photo", "red");
        addLabeledSection(photoSections, pendingPane, "⌛ Pending", "orange");
        addLabeledSection(photoSections, approvedPane, "✅ Approved", "green");

        return photoSections;
    }

    private void addLabeledSection(VBox container, FlowPane pane, String labelText, String color) {
        if (!pane.getChildren().isEmpty()) {
            Label label = new Label(labelText);
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
            container.getChildren().addAll(label, pane);
        }
    }

    private Button createAddPhotoButton(Item item, Order order) {
        Button addPhotoButton = new Button("Add Photo");
        addPhotoButton.setStyle("-fx-background-color: #004b88; -fx-background-radius: 8");
        addPhotoButton.setTextFill(javafx.scene.paint.Color.WHITE);
        addPhotoButton.setOnAction(event ->
                imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber())
        );
        return addPhotoButton;
    }
}