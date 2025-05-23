package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import dk.easv.blsgn.intgrpbelsign.utils.ImageOverlayUtil;
import dk.easv.blsgn.intgrpbelsign.utils.PdfReportGenerator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class QC {

    private final OrderModel orderModel;
    private final UserModel userModel;
    private final ImageOverlayUtil imageOverlayUtil;
    private final Set<String> submittedItems = new HashSet<>();

    public QC() {
        this.orderModel = new OrderModel(new OrderManager());
        this.imageOverlayUtil = new ImageOverlayUtil(new OrderManager());
        this.userModel = new UserModel(new UserManager());
    }

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    private List<Order> allOrders;
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        setupSearchAndSelection();
    }

    private void setupSearchAndSelection() {
        allOrders = orderModel.getAllOrders();
        displayOrd(allOrders);
        flowPane.getChildren().clear();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<Order> filtered = allOrders.stream()
                    .filter(order -> order.getOrderNumber().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(Collectors.toList());

            displayOrd(filtered);
            flowPane.getChildren().clear();
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
        listView.setItems(orderModel.getOrderNumbers(orders));
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

                if (order != null && hasPendingImages(order)) {
                    setStyle("-fx-border-color: #f19352; -fx-border-width: 2px; -fx-border-radius: 3px;");
                } else {
                    setStyle("-fx-background-insets: 0 0 3px 0;");
                }
            }
        });
    }

    private boolean hasPendingImages(Order order) {
        for (Item item : order.getItems()) {
            List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
            if (images.stream().anyMatch(img -> "pending".equalsIgnoreCase(img.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    private void displayOrders(List<Order> orders) {
        flowPane.getChildren().clear();
        for (Order order : orders) {
            VBox orderBox = new VBox(10);
            orderBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
            orderBox.setPrefWidth(700);

            Label orderLabel = new Label("Order: " + order.getOrderNumber());
            orderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            VBox itemsContainer = new VBox(15);
            for (Item item : order.getItems()) {
                VBox itemBox = new VBox(10);
                itemBox.setAlignment(Pos.TOP_LEFT);
                itemBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                itemBox.setPrefWidth(650);

                HBox titleBar = new HBox();
                titleBar.setAlignment(Pos.CENTER_LEFT);
                Label itemNameLabel = new Label(item.getItemName() + " ▼");
                itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
                titleBar.getChildren().add(itemNameLabel);

                VBox photoContent = new VBox(10);
                photoContent.setVisible(true);
                photoContent.setManaged(true);

                itemNameLabel.setOnMouseClicked(event -> {
                    boolean visible = photoContent.isVisible();
                    photoContent.setVisible(!visible);
                    photoContent.setManaged(!visible);
                    itemNameLabel.setText(item.getItemName() + (visible ? " ▲" : " ▼"));
                });

                GridPane anglesPane = new GridPane();
                anglesPane.setHgap(10);
                anglesPane.setVgap(10);
                Map<String, Integer> angleCols = Map.of(
                        "front", 0, "back", 1, "top", 2, "right", 3, "left", 4
                );
                FlowPane extraPhotos = new FlowPane(10, 10);

                List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
                for (ImageWithMeta meta : images) {
                    VBox imageContainer = createImageCard(meta, item, order);
                    String angle = meta.getViewType() != null ? meta.getViewType().toLowerCase() : "extra";
                    if (angleCols.containsKey(angle)) {
                        anglesPane.add(imageContainer, angleCols.get(angle), 0);
                    } else {
                        extraPhotos.getChildren().add(imageContainer);
                    }
                }

                photoContent.getChildren().addAll(anglesPane, new Label("Extra Photos:"), extraPhotos);

                if (!item.isSubmitted()) {
                    orderModel.markItemAsSubmitted(item.getId());
                    Button submitButton = new Button("Submit");
                    submitButton.setStyle("-fx-background-color: #3a86ff; -fx-text-fill: white;");
                    submitButton.setOnAction(e -> {
                        boolean hasPending = orderModel.getAllImagesWithStatus(order.getID(), item.getId())
                                .stream()
                                .anyMatch(meta -> "pending".equalsIgnoreCase(meta.getStatus()));
                        if (hasPending) {
                            showAlert("You must review all images for this item before submitting.");
                            return;
                        }
                        submittedItems.add(order.getOrderNumber() + ":" + item.getId());
                        displayOrders(List.of(order));
                    });
                    photoContent.getChildren().add(submitButton);
                }

                itemBox.getChildren().addAll(titleBar, photoContent);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }

    private VBox createImageCard(ImageWithMeta meta, Item item, Order order) {
        VBox imageContainer = new VBox(5);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");

        Label angleLabel = new Label(meta.getViewType());
        angleLabel.setStyle("-fx-font-weight: bold;");

        Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(150);
        imgView.setFitHeight(150);
        imgView.setPreserveRatio(true);

        StackPane imageStack = imageOverlayUtil.createImageWithOverlay(
                imgView, img, meta.getStatus(), meta.getViewType(), meta, item, order.getID(), order.getOrderNumber(), true
        );

        Label statusLabel = new Label("Status: " + meta.getStatus());
        statusLabel.setStyle(getStatusStyle(meta.getStatus()));

        imageContainer.getChildren().addAll(angleLabel, imageStack, statusLabel);

        String itemKey = order.getOrderNumber() + ":" + item.getId();
        if (!submittedItems.contains(itemKey)) {
            Button approve = new Button("\u2705");
            Button reject = new Button("\u274C");
            approve.setStyle("-fx-background-color: #8ad38c;");
            reject.setStyle("-fx-background-color: #fb7e77;");

            approve.setOnAction(e -> {
                orderModel.updateImageStatus(meta.getId(), "approved");
                statusLabel.setText("Status: approved");
                statusLabel.setStyle(getStatusStyle("approved"));
                displayOrd(allOrders);
            });

            reject.setOnAction(e -> {
                orderModel.updateImageStatus(meta.getId(), "rejected");
                statusLabel.setText("Status: rejected");
                statusLabel.setStyle(getStatusStyle("rejected"));
                displayOrd(allOrders);
            });

            HBox buttonsBox = new HBox(10, approve, reject);
            buttonsBox.setAlignment(Pos.CENTER);
            imageContainer.getChildren().add(buttonsBox);
        }

        return imageContainer;
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> "-fx-text-fill: green; -fx-font-size: 15px";
            case "rejected" -> "-fx-text-fill: red; -fx-font-size: 15px";
            default -> "-fx-text-fill: orange; -fx-font-size: 15px";
        };
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onPreviewReport() {
        try {
            String selectedOrderNumber = listView.getSelectionModel().getSelectedItem();
            if (selectedOrderNumber == null) {
                showAlert("Please select an order first.");
                return;
            }

            Order selectedOrder = allOrders.stream()
                    .filter(o -> o.getOrderNumber().equals(selectedOrderNumber))
                    .findFirst()
                    .orElse(null);

            if (selectedOrder == null || selectedOrder.getItems().isEmpty()) {
                showAlert("No items found for selected order.");
                return;
            }

            for (Item item : selectedOrder.getItems()) {
                List<ImageWithMeta> imageMetas = orderModel.getAllImagesWithStatus(selectedOrder.getID(), item.getId());

                boolean hasNonApproved = imageMetas.stream()
                        .anyMatch(img -> !"approved".equalsIgnoreCase(img.getStatus()));

                if (hasNonApproved) {
                    showAlert("You can only preview the report when all images are approved.");
                    return;
                }
            }

            List<byte[]> approvedImages = new ArrayList<>();
            for (Item item : selectedOrder.getItems()) {
                List<ImageWithMeta> imageMetas = orderModel.getAllImagesWithStatus(selectedOrder.getID(), item.getId());
                approvedImages.addAll(
                        imageMetas.stream()
                                .filter(img -> "approved".equalsIgnoreCase(img.getStatus()))
                                .map(ImageWithMeta::getImageData)
                                .toList());
            }

            if (approvedImages.isEmpty()) {
                showAlert("No approved images for this order.");
                return;
            }

            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("dk/easv/blsgn/intgrpbelsign/Pictures/icons/logo.png");
            if (logoStream == null) {
                showAlert("Belman logo not found.");
                return;
            }
            byte[] logoBytes = logoStream.readAllBytes();

            byte[] qcSignature = userModel.getSignatureForUser(currentUser.getUser_id());
            if (qcSignature == null) {
                showAlert("QC signature not found.");
                return;
            }

            byte[] pdf = PdfReportGenerator.generatePdfWithImages(
                    selectedOrder.getOrderNumber(),
                    approvedImages,
                    logoBytes,
                    qcSignature
            );

            new PdfPreviewDialog(pdf, selectedOrder.getOrderNumber()).showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to generate or preview PDF.");
        }
    }
}