package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.gui.controllers.PdfPreviewDialog;
import dk.easv.blsgn.intgrpbelsign.model.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.utils.PdfReportGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

public class QC {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField searchField;

    @FXML
    private Button detailsButton;

    private final OrderManager orderManager = new OrderManager();

    private List<Order> allOrders;

    @FXML
    public void initialize() {
        setupSearchAndSelection();
        updateDetailsButtonStyle(allOrders);
    }

    @FXML
    private void setupSearchAndSelection() {
        allOrders = orderManager.getAllOrders();

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
                updateDetailsButtonStyle(selected);
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
            VBox orderBox = new VBox(10);
            orderBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
            orderBox.setPrefWidth(700);

            Label orderLabel = new Label("Order: " + order.getOrderNumber());
            orderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            VBox itemsContainer = new VBox(15);
            for (Item item : order.getItems()) {
                VBox itemBox = new VBox(5);
                itemBox.setAlignment(Pos.TOP_LEFT);
                itemBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                itemBox.setPrefWidth(650);

                Label itemNameLabel = new Label(item.getItemName());
                itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

                FlowPane photoPane = new FlowPane(10, 10);
                photoPane.setPrefWrapLength(600);

                List<ImageWithMeta> images = orderManager.getAllImagesWithStatus(order.getID(), item.getId());

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
                        orderManager.updateImageStatus(meta.getId(), "approved");
                        statusLabel.setText("Status: approved");
                        statusLabel.setStyle(getStatusStyle("approved"));
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        updateDetailsButtonStyle(allOrders);
                    });

                    rejectBtn.setOnAction(ev -> {
                        orderManager.updateImageStatus(meta.getId(), "rejected");
                        statusLabel.setText("Status: rejected");
                        statusLabel.setStyle(getStatusStyle("rejected"));
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                        updateDetailsButtonStyle(allOrders);
                    });

                    if ("approved".equalsIgnoreCase(meta.getStatus()) || "rejected".equalsIgnoreCase(meta.getStatus())) {
                        approveBtn.setVisible(false);
                        rejectBtn.setVisible(false);
                    }

                    VBox imageBox = new VBox(5, imgView, statusLabel, new HBox(10, approveBtn, rejectBtn));
                    imageBox.setAlignment(Pos.CENTER);
                    photoPane.getChildren().add(imageBox);
                }

                itemBox.getChildren().addAll(itemNameLabel, photoPane);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }

    private String getStatusStyle(String status) {
        return switch (status.toLowerCase()) {
            case "approved" -> "-fx-text-fill: green; -fx-font-size: 15px";
            case "rejected" -> "-fx-text-fill: red; -fx-font-size: 15px";
            default -> "-fx-text-fill: orange; -fx-font-size: 15px";
        };
    }

    private void updateDetailsButtonStyle(List<Order> orders) {
        boolean hasPending = false;

        for (Order order : orders) {
            for (Item item : order.getItems()) {
                List<ImageWithMeta> images = orderManager.getAllImagesWithStatus(order.getID(), item.getId());
                if (images.stream().anyMatch(img -> "pending".equalsIgnoreCase(img.getStatus()))) {
                    hasPending = true;
                    break;
                }
            }
            if (hasPending) break;
        }

        if (hasPending) {
            detailsButton.setStyle("-fx-background-color: #FF5252; -fx-text-fill: white;");
        } else {
            detailsButton.setStyle(""); // Reset style
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
                    .findFirst()
                    .orElse(null);

            if (selectedOrder == null || selectedOrder.getItems().isEmpty()) {
                showAlert("No items found for selected order.");
                return;
            }

            Item selectedItem = selectedOrder.getItems().get(0);
            List<byte[]> images = orderManager.getImagesForItem(selectedOrder.getID(), selectedItem.getId());

            byte[] pdf = PdfReportGenerator.generatePdfWithImages(
                    selectedOrder.getOrderNumber(),
                    selectedItem,
                    images
            );

            PdfPreviewDialog preview = new PdfPreviewDialog(pdf, selectedOrder.getOrderNumber());
            preview.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to generate or preview PDF.");
        }
    }

    @FXML
    private void onDetailsButtonClick() {
        List<Order> pendingOrders = allOrders.stream()
                .filter(order -> order.getItems().stream()
                        .anyMatch(item -> orderManager.getAllImagesWithStatus(order.getID(), item.getId()).stream()
                                .anyMatch(img -> "pending".equalsIgnoreCase(img.getStatus()))))
                .collect(Collectors.toList());

        if (pendingOrders.isEmpty()) {
            showAlert("No pending images found.");
            return;
        }

        ContextMenu contextMenu = new ContextMenu();

        for (Order order : pendingOrders) {
            MenuItem item = new MenuItem("Order: " + order.getOrderNumber());
            item.setOnAction(ev -> {
                listView.getSelectionModel().select(order.getOrderNumber());
            });
            contextMenu.getItems().add(item);
        }

        // Show the menu anchored to the button
        contextMenu.show(detailsButton, Side.BOTTOM, 0, 0);
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
