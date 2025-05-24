package dk.easv.blsgn.intgrpbelsign.gui.controllers.operator;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.gui.controllers.SharableOPQC.BaseOrderController;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import dk.easv.blsgn.intgrpbelsign.utils.ImageOverlayUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.util.*;

public class OperatorController extends BaseOrderController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    private final OrderModel orderModel = new OrderModel(new OrderManager());
    private final ImageOverlayUtil imageOverlayUtil = new ImageOverlayUtil(new OrderManager());

    private List<Order> allOrders;

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
        allOrders = orderModel.getAllOrders();
        displayOrd(allOrders);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Order> filtered = orderModel.filterOrders(newVal);
            displayOrd(filtered);
        });

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
                        .findFirst()
                        .orElse(null);

                if (order != null && hasRejectedImages(order)) {
                    setStyle("-fx-border-color: #f19352; -fx-border-width: 2px; -fx-border-radius: 3px;");
                } else {
                    setStyle("-fx-background-insets: 0 0 3px 0;");
                }
            }
        });

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selectedOrderNumber) -> {
            if (selectedOrderNumber != null) {
                List<Order> selected = orderModel.findOrdersByNumber(selectedOrderNumber);
                displayOrders(selected);
            }
        });
    }

    public boolean hasRejectedImages(Order order) {
        for (Item item : order.getItems()) {
            List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
            if (images.stream().anyMatch(img -> "rejected".equalsIgnoreCase(img.getStatus()))) {
                return true;
            }
        }
        return false;
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

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        Label itemNameLabel = new Label(item.getItemName() + " ▼");
        itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        titleBar.getChildren().add(itemNameLabel);

        VBox photoSections = createPhotoSections(item, order);
        photoSections.setVisible(true);
        photoSections.setManaged(true);

        itemNameLabel.setOnMouseClicked(event -> {
            boolean visible = photoSections.isVisible();
            photoSections.setVisible(!visible);
            photoSections.setManaged(!visible);
            itemNameLabel.setText(item.getItemName() + (visible ? " ▲" : " ▼"));
        });

        itemBox.getChildren().addAll(titleBar, photoSections);
        return itemBox;
    }

    private VBox createPhotoSections(Item item, Order order) {
        VBox photoSections = new VBox(10);

        String[] angles = {"Front", "Back", "Top", "Right", "Left"};
        GridPane anglesPane = new GridPane();
        anglesPane.setHgap(10);
        anglesPane.setVgap(10);

        Map<String, Integer> angleToColumn = new HashMap<>();
        for (int i = 0; i < angles.length; i++) {
            String angle = angles[i];
            angleToColumn.put(angle.toLowerCase(), i);

            Label label = new Label(angle);
            label.setStyle("-fx-font-weight: bold;");

            Node placeholder = createPlaceholderSlot(item, order, angle);

            VBox card = new VBox(5, label, placeholder);
            card.setAlignment(Pos.TOP_CENTER);
            card.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

            anglesPane.add(card, i, 0);
        }

        List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
        for (ImageWithMeta meta : images) {
            String viewType = meta.getViewType() != null ? meta.getViewType().toLowerCase() : "";
            if (angleToColumn.containsKey(viewType)) {
                int col = angleToColumn.get(viewType);
                Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
                ImageView imgView = new ImageView(img);

                StackPane imageWithOverlay = imageOverlayUtil.createImageWithOverlay(
                        imgView, img, meta.getStatus(), meta.getViewType(), meta, item, order.getID(), order.getOrderNumber(), false
                );

                VBox updatedCard = new VBox(5, new Label(meta.getViewType()), imageWithOverlay);
                updatedCard.setAlignment(Pos.TOP_CENTER);
                updatedCard.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

                Node toRemove = null;
                for (Node node : anglesPane.getChildren()) {
                    if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == 0) {
                        toRemove = node;
                        break;
                    }
                }
                if (toRemove != null) anglesPane.getChildren().remove(toRemove);
                anglesPane.add(updatedCard, col, 0);
            }
        }

        FlowPane extraPhotosPane = new FlowPane(10, 10);
        for (ImageWithMeta meta : images) {
            String viewType = meta.getViewType() != null ? meta.getViewType().toLowerCase() : "";
            if (!angleToColumn.containsKey(viewType)) {
                Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
                ImageView imgView = new ImageView(img);
                StackPane imageStack = imageOverlayUtil.createImageWithOverlay(
                        imgView, img, meta.getStatus(), meta.getViewType(), meta, item, order.getID(), order.getOrderNumber(), false
                );

                VBox card = new VBox(imageStack);
                card.setAlignment(Pos.CENTER);
                card.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-width: 1;");

                extraPhotosPane.getChildren().add(card);
            }
        }

        Button addExtraPhoto = new Button("+");
        addExtraPhoto.setStyle("-fx-font-size: 20px; -fx-min-width: 100px; -fx-min-height: 100px; -fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ccc; -fx-border-width: 1;");
        addExtraPhoto.setOnAction(e -> imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber(), "Extra"));
        extraPhotosPane.getChildren().add(addExtraPhoto);

        Label extraLabel = new Label("Extra Photos");
        extraLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        photoSections.getChildren().addAll(anglesPane, extraLabel, extraPhotosPane);
        return photoSections;
    }

    private Button createPlaceholderSlot(Item item, Order order, String angle) {
        Button plusButton = new Button("+");
        plusButton.setPrefSize(100, 100);
        plusButton.setStyle("-fx-font-size: 20px; -fx-border-width: 1; -fx-cursor: hand; -fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-border-color: #ccc;");

        plusButton.setOnAction(e -> imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber(), angle));
        return plusButton;
    }
}
