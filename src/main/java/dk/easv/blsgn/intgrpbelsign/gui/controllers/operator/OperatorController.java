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
import java.util.*;

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

        //Button addPhotoButton = createAddPhotoButton(item, order);

        itemBox.getChildren().addAll(itemNameLabel, photoSections/*,addPhotoButton*/);
        return itemBox;
    }

    private VBox createPhotoSections(Item item, Order order) {
        VBox photoSections = new VBox(10);

        String[] angles = {"Front", "Back", "Top", "Right", "Left"};
        Map<String, VBox> angleSlotMap = new LinkedHashMap<>();
        GridPane anglesPane = new GridPane();
        anglesPane.setHgap(10);
        anglesPane.setVgap(10);

        for (int i = 0; i < angles.length; i++) {
            String angle = angles[i];
            VBox slot = createEmptySlot(item, order, angle);
            angleSlotMap.put(angle.toLowerCase(), slot);
            anglesPane.add(slot, i % 5, i / 5);
        }

        FlowPane extraPhotosPane = new FlowPane(10, 10);

        List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
        for (ImageWithMeta meta : images) {
            Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(100);
            imgView.setFitHeight(100);
            imgView.setPreserveRatio(true);

            StackPane stack = imageOverlayUtil.createImageWithOverlay(
                    imgView, img, meta.getStatus(), meta.getViewType(), meta, item, order.getID(), order.getOrderNumber(), false
            );

            String angle = meta.getViewType() != null ? meta.getViewType().toLowerCase() : "";
            VBox slot = angleSlotMap.get(angle);
            if (slot != null) {
                slot.getChildren().set(1, stack);
            } else {
                extraPhotosPane.getChildren().add(stack);
            }
        }

        Label anglesLabel = new Label("Standard Views");
        anglesLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label extraLabel = new Label("Extra Photos");
        extraLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Button addExtraPhoto = new Button("+");
        addExtraPhoto.setStyle("-fx-font-size: 20px; -fx-min-width: 100px; -fx-min-height: 100px;");
        addExtraPhoto.setOnAction(e -> imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber(), "Extra"));
        extraPhotosPane.getChildren().add(addExtraPhoto);

        photoSections.getChildren().addAll(anglesLabel, anglesPane, extraLabel, extraPhotosPane);
        return photoSections;
    }

    private VBox createEmptySlot(Item item, Order order, String angle) {
        VBox slot = new VBox(5);
        slot.setAlignment(Pos.CENTER);

        Label label = new Label(angle);
        label.setStyle("-fx-font-weight: bold;");

        StackPane plusSlot = new StackPane();
        plusSlot.setPrefSize(100, 100);
        plusSlot.setStyle("-fx-background-color: #ddd; -fx-border-color: black; -fx-alignment: center;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 30px;");
        plusSlot.getChildren().add(plus);

        plusSlot.setOnMouseClicked(e -> imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber(), angle));

        slot.getChildren().addAll(label, plusSlot);
        return slot;
    }

    /*private Button createAddPhotoButton(Item item, Order order) {
        Button addPhotoButton = new Button("Add Photo");
        addPhotoButton.setStyle("-fx-background-color: #004b88; -fx-background-radius: 8");
        addPhotoButton.setTextFill(javafx.scene.paint.Color.WHITE);
        addPhotoButton.setOnAction(event ->
                imageOverlayUtil.openCameraWindow(item, order.getID(), order.getOrderNumber(), "Extra")
        );
        return addPhotoButton;
    }*/
}
