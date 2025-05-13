package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.gui.controllers.PdfPreviewDialog;
import dk.easv.blsgn.intgrpbelsign.utils.PdfReportGenerator;
import dk.easv.blsgn.intgrpbelsign.model.ImageWithMeta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    private final OrderManager orderManager = new OrderManager();

    private List<Order> allOrders;

    @FXML
    public void initialize() {
        setupSearchAndSelection();
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
            displayOrders(filtered);
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

                FlowPane photoPane = new FlowPane(5, 5);
                photoPane.setPrefWrapLength(600);

                List<ImageWithMeta> images = orderManager.getAllImagesWithStatus(order.getID(), item.getId());

                for (ImageWithMeta meta : images) {
                    Image img = new Image(new ByteArrayInputStream(meta.getImageData()));
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(150);
                    imgView.setFitHeight(150);
                    imgView.setPreserveRatio(true);

                    Label statusLabel = new Label("Status: " + meta.getStatus());

                    Button approveBtn = new Button("✅");
                    Button rejectBtn = new Button("❌");

                    approveBtn.setOnAction(ev -> {
                        orderManager.updateImageStatus(meta.getIndex(), "approved");
                        statusLabel.setText("Status: approved");
                    });

                    rejectBtn.setOnAction(ev -> {
                        orderManager.updateImageStatus(meta.getIndex(), "rejected");
                        statusLabel.setText("Status: rejected");
                    });

                    VBox imageBox = new VBox(5, imgView, statusLabel, new HBox(5, approveBtn, rejectBtn));
                    photoPane.getChildren().add(imageBox);
                }

                itemBox.getChildren().addAll(itemNameLabel, photoPane);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
