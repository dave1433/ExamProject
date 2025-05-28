package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.gui.controllers.SharableOPQC.BaseOrderController;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import dk.easv.blsgn.intgrpbelsign.utils.ImageOverlayUtil;
import dk.easv.blsgn.intgrpbelsign.utils.PdfReportGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class QC extends BaseOrderController {

    private final OrderModel orderModel = new OrderModel(new OrderManager());
    private final UserModel userModel = new UserModel(new UserManager());
    private final ImageOverlayUtil imageOverlayUtil = new ImageOverlayUtil(new OrderManager());

    @FXML
    private FlowPane flowPane;
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField searchField;
    @FXML
    private Button reportPreviewButton;

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
                        .findFirst().orElse(null);

                if (order != null && hasPendingImages(order)) {
                    setStyle("-fx-border-color: #f19352; -fx-border-width: 2px; -fx-border-radius: 3px;");
                } else {
                    setStyle("-fx-background-insets: 0 0 3px 0;");
                }
            }
        });
    }

    private boolean hasPendingImages(Order order) {
        return order.getItems().stream()
                .flatMap(item -> orderModel.getAllImagesWithStatus(order.getID(), item.getId()).stream())
                .anyMatch(img -> "pending".equalsIgnoreCase(img.getStatus()));
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

                Label itemNameLabel = new Label(item.getItemName() + " ▼");
                itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

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
                Map<String, Integer> angleCols = Map.of("front", 0, "back", 1, "top", 2, "right", 3, "left", 4);

                FlowPane extraPhotos = new FlowPane(10, 10);
                Button submitButton = new Button();
                submitButton.setPrefWidth(100);

                List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
                for (ImageWithMeta meta : images) {
                    VBox imageContainer = createImageCard(meta, item, order, submitButton);
                    String angle = Optional.ofNullable(meta.getViewType()).orElse("extra").toLowerCase();
                    if (angleCols.containsKey(angle)) {
                        anglesPane.add(imageContainer, angleCols.get(angle), 0);
                    } else {
                        extraPhotos.getChildren().add(imageContainer);
                    }
                }

                photoContent.getChildren().addAll(anglesPane, new Label("Extra Photos:"), extraPhotos);
                updateSubmitButtonState(item, order, submitButton);

                submitButton.setOnAction(e -> {
                    List<ImageWithMeta> imgs = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
                    boolean allReviewed = imgs.stream().allMatch(img -> orderModel.hasTempStatus(img.getId()));
                    if (!allReviewed) {
                        showAlert("You must review all images before submitting.");
                        return;
                    }
                    for (ImageWithMeta img : imgs) {
                        String newStatus = orderModel.getTempStatus(img.getId());
                        orderModel.updateImageStatus(img.getId(), newStatus);
                    }
                    orderModel.clearTempStatusesForItem(order.getID(), item.getId());
                    orderModel.markItemAsSubmitted(order.getID(), item.getId());
                    item.setSubmitted(true);
                    updateSubmitButtonState(item, order, submitButton);
                    displayOrders(List.of(order));
                });

                photoContent.getChildren().add(submitButton);

                HBox titleBar = new HBox(itemNameLabel);
                titleBar.setAlignment(Pos.CENTER_LEFT);
                itemBox.getChildren().addAll(titleBar, photoContent);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }

    private VBox createImageCard(ImageWithMeta meta, Item item, Order order, Button submitButton) {
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

        String shownStatus = orderModel.getTempStatus(meta.getId());
        if (shownStatus == null) shownStatus = meta.getStatus();
        Label statusLabel = new Label("Status: " + shownStatus);
        statusLabel.setStyle(getStatusStyle(shownStatus));

        Button approve = new Button("\u2705");
        Button reject = new Button("\u274C");
        approve.setStyle("-fx-background-color: #8ad38c;");
        reject.setStyle("-fx-background-color: #fb7e77;");

        approve.setOnAction(e -> {
            orderModel.setTempStatus(meta.getId(), "approved");
            statusLabel.setText("Status: approved");
            statusLabel.setStyle(getStatusStyle("approved"));
            updateSubmitButtonState(item, order, submitButton);
        });

        reject.setOnAction(e -> {
            orderModel.setTempStatus(meta.getId(), "rejected");
            statusLabel.setText("Status: rejected");
            statusLabel.setStyle(getStatusStyle("rejected"));
            updateSubmitButtonState(item, order, submitButton);
        });

        HBox buttonsBox = new HBox(10, approve, reject);
        buttonsBox.setAlignment(Pos.CENTER);

        if (item.isSubmitted()) {
            buttonsBox.setVisible(false);
            buttonsBox.setManaged(false);
        }

        imageContainer.getChildren().addAll(angleLabel, imageStack, statusLabel, buttonsBox);
        return imageContainer;
    }

    private void updateSubmitButtonState(Item item, Order order, Button submitButton) {
        List<ImageWithMeta> allImages = orderModel.getAllImagesWithStatus(order.getID(), item.getId());
        boolean allReviewed = allImages.stream().allMatch(img -> orderModel.hasTempStatus(img.getId()));

        if (!allReviewed) {
            submitButton.setDisable(true);
            submitButton.setText("Submit");
            submitButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
            item.setSubmitted(false);
            orderModel.markItemAsUnsubmitted(order.getID(), item.getId());
            return;
        }

        if (!item.isSubmitted()) {
            submitButton.setDisable(false);
            submitButton.setText("Submit");
            submitButton.setStyle("-fx-background-color: #3a86ff; -fx-text-fill: white;");
        } else {
            submitButton.setDisable(true);
            submitButton.setText("\u2713 Submitted");
            submitButton.setStyle("-fx-background-color: #8ad38c; -fx-text-fill: white; -fx-font-weight: bold;");
        }
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
                    .findFirst().orElse(null);

            if (selectedOrder == null || selectedOrder.getItems().isEmpty()) {
                showAlert("No items found for selected order.");
                return;
            }

            // Ensure all images are approved
            for (Item item : selectedOrder.getItems()) {
                boolean hasNonApproved = orderModel.getAllImagesWithStatus(selectedOrder.getID(), item.getId())
                        .stream().anyMatch(img -> !"approved".equalsIgnoreCase(img.getStatus()));
                if (hasNonApproved) {
                    showAlert("You can only preview the report when all images are approved.");
                    return;
                }
            }

            // Build item → approved image list map
            Map<Item, List<ImageWithMeta>> itemImages = new HashMap<>();
            for (Item item : selectedOrder.getItems()) {
                List<ImageWithMeta> approved = orderModel.getAllImagesWithStatus(selectedOrder.getID(), item.getId())
                        .stream().filter(img -> "approved".equalsIgnoreCase(img.getStatus())).toList();
                if (!approved.isEmpty()) {
                    itemImages.put(item, approved);
                }
            }

            if (itemImages.isEmpty()) {
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
                    itemImages,
                    logoBytes,
                    qcSignature,
                    currentUser.getFirst_name() + " " + currentUser.getLast_name()
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dk/easv/blsgn/intgrpbelsign/ReportPreviewPane.fxml"));
            Parent previewPane = loader.load();

            PdfPreviewDialog controller = loader.getController();
            controller.initData(pdf, selectedOrder.getOrderNumber(), this); // pass QC controller

            flowPane.getChildren().setAll(previewPane);

            // ✅ Hide the preview button
            reportPreviewButton.setVisible(false);
            reportPreviewButton.setDisable(true);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to generate or preview PDF.");
        }
    }

    public void returnFromPreview() {
        reportPreviewButton.setVisible(true);
        reportPreviewButton.setDisable(false);

        // Show the currently selected order again
        String selectedOrderNumber = listView.getSelectionModel().getSelectedItem();
        if (selectedOrderNumber != null) {
            List<Order> selected = allOrders.stream()
                    .filter(order -> order.getOrderNumber().equals(selectedOrderNumber))
                    .toList();
            displayOrders(selected);
        }
    }

    public String getStatusStyle(String status) {
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
}
