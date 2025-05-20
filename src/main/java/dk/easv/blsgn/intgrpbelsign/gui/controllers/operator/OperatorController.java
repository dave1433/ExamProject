package dk.easv.blsgn.intgrpbelsign.gui.controllers.operator;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import dk.easv.blsgn.intgrpbelsign.utils.ImageOverlayUtil;
import java.io.ByteArrayInputStream;
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
    private final ImageOverlayUtil imageOverlayUtil;
    private List<Order> allOrders;

    public OperatorController() {
        imageOverlayUtil = new ImageOverlayUtil(orderManager);
        imageOverlayUtil.setRefreshCallback(this::refreshView);
    }

    private void refreshView() {
        String selectedOrderNumber = listView.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber != null) {
            List<Order> selected = allOrders.stream()
                    .filter(order -> order.getOrderNumber().equals(selectedOrderNumber))
                    .collect(Collectors.toList());
            displayOrders(selected);
        }
    }

    @FXML
    public void initialize() {
        onSearchFilter();
    }

    @FXML
    private void onSearchFilter() {
        allOrders = orderManager.getAllOrders();
        displayOrd(allOrders);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Order> filtered = allOrders.stream()
                    .filter(order -> order.getOrderNumber().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());
            displayOrd(filtered);
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedOrderNumber) -> {
            if (selectedOrderNumber != null) {
                List<Order> selected = allOrders.stream()
                        .filter(order -> order.getOrderNumber().equals(selectedOrderNumber))
                        .collect(Collectors.toList());
                displayOrders(selected);
            }
        });
    }

    private void displayOrd(List<Order> orders) {
        ObservableList<String> orderNumbers = FXCollections.observableArrayList();
        for (Order order : orders) {
            orderNumbers.add(order.getOrderNumber());
        }
        listView.setItems(orderNumbers);
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

        List<ImageWithMeta> images = orderManager.getAllImagesWithStatus(order.getID(), item.getId());

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
                    order.getOrderNumber()
            );

            switch (meta.getStatus().toLowerCase()) {
                case "rejected" -> rejectedPane.getChildren().add(imageStack);
                case "approved" -> approvedPane.getChildren().add(imageStack);
                default -> pendingPane.getChildren().add(imageStack);
            }
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