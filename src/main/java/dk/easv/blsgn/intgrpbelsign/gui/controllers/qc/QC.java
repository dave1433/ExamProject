package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.be.User;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.bll.UserManager;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.model.OrderModel;
import dk.easv.blsgn.intgrpbelsign.model.UserModel;
import dk.easv.blsgn.intgrpbelsign.utils.PdfReportGenerator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QC {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    private final UserModel userModel = new UserModel(new UserManager());  // UserModel instance to get the QC's signature
    private final OrderModel orderModel = new OrderModel(new OrderManager());

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
                    setStyle("-fx-border-color: #FF5252; -fx-border-width: 2px; -fx-border-radius: 3px;");
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
            for (Item item : order.getItems()) {
                VBox itemBox = new VBox(5);
                itemBox.setAlignment(Pos.TOP_LEFT);
                itemBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                itemBox.setPrefWidth(650);

                Label itemNameLabel = new Label(item.getItemName());
                itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
                //itemNameLabel.setStyle("-fx-background-color: #D9D9D9");


                FlowPane photoPane = new FlowPane(10, 10);
                photoPane.setPrefWrapLength(600);
                photoPane.setVisible(false);
                photoPane.setManaged(false);

                List<ImageWithMeta> images = orderModel.getAllImagesWithStatus(order.getID(), item.getId());

                for (ImageWithMeta meta : images) {
                    Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(150);
                    imgView.setFitHeight(150);
                    imgView.setPreserveRatio(true);

                    Label statusLabel = new Label("Status: " + meta.getStatus());
                    statusLabel.setStyle(getStatusStyle(meta.getStatus()));

                    Button approveBtn = new Button("✅");
                    Button rejectBtn = new Button("❌");

                    approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                    rejectBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

                    Tooltip.install(approveBtn, new Tooltip("Approve this image"));
                    Tooltip.install(rejectBtn, new Tooltip("Reject this image"));

                    approveBtn.setOnAction(ev -> {
                        orderModel.updateImageStatus(meta.getId(), "approved");
                        statusLabel.setText("Status: approved");
                        statusLabel.setStyle(getStatusStyle("approved"));
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        displayOrd(allOrders); // refresh ListView styles
                    });

                    rejectBtn.setOnAction(ev -> {
                        orderModel.updateImageStatus(meta.getId(), "rejected");
                        statusLabel.setText("Status: rejected");
                        statusLabel.setStyle(getStatusStyle("rejected"));
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        displayOrd(allOrders); // refresh ListView styles
                    });

                    if ("approved".equalsIgnoreCase(meta.getStatus()) || "rejected".equalsIgnoreCase(meta.getStatus())) {
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                    }

                    VBox imageBox = new VBox(5, imgView, statusLabel, new HBox(10, approveBtn, rejectBtn));
                    imageBox.setAlignment(Pos.CENTER);
                    photoPane.getChildren().add(imageBox);
                }

                // Toggle visibility on click
                itemNameLabel.setOnMouseClicked(ev -> {
                    boolean visible = photoPane.isVisible();
                    photoPane.setVisible(!visible);
                    photoPane.setManaged(!visible);
                });

                itemBox.getChildren().addAll(itemNameLabel, photoPane);
                flowPane.getChildren().add(itemBox);
            }
        }
    }


    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> "-fx-text-fill: green; -fx-font-size: 15px";
            case "rejected" -> "-fx-text-fill: red; -fx-font-size: 15px";
            default -> "-fx-text-fill: orange; -fx-font-size: 15px";
        };
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

            // ❗ Check: Are all images approved?
            for (Item item : selectedOrder.getItems()) {
                List<ImageWithMeta> imageMetas = orderManager.getAllImagesWithStatus(selectedOrder.getID(), item.getId());

                boolean hasNonApproved = imageMetas.stream()
                        .anyMatch(img -> !"approved".equalsIgnoreCase(img.getStatus()));

                if (hasNonApproved) {
                    showAlert("You can only preview the report when all images are approved.");
                    return;
                }
            }

            // ✅ All approved → collect them
            List<byte[]> approvedImages = new ArrayList<>();
            for (Item item : selectedOrder.getItems()) {
                List<ImageWithMeta> imageMetas = orderManager.getAllImagesWithStatus(selectedOrder.getID(), item.getId());

                approvedImages.addAll(
                        imageMetas.stream()
                                .filter(img -> "approved".equalsIgnoreCase(img.getStatus()))
                                .map(ImageWithMeta::getImageData)
                                .collect(Collectors.toList()));
            }

            if (approvedImages.isEmpty()) {
                showAlert("No approved images for this order.");
                return;
            }

            // Load logo
            InputStream logoStream = getClass().getClassLoader().getResourceAsStream("dk/easv/blsgn/intgrpbelsign/Pictures/icons/1.png");
            if (logoStream == null) {
                showAlert("Belman logo not found.");
                return;
            }
            byte[] logoBytes = logoStream.readAllBytes();

            // Load QC signature
            byte[] qcSignature = userModel.getSignatureForUser(currentUser.getUser_id());
            if (qcSignature == null) {
                showAlert("QC signature not found.");
                return;
            }

            // Generate & preview the PDF
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
