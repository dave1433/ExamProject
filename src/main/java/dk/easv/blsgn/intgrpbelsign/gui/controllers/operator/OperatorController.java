package dk.easv.blsgn.intgrpbelsign.gui.controllers.operator;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.model.ImageWithMeta;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
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

    private List<Order> allOrders;

    @FXML
    public void initialize() {
        onSearchFilter();
    }

    @FXML
    private void onSearchFilter(){
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

                // Containers for grouped images
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

                    switch (meta.getStatus().toLowerCase()) {
                        case "rejected" -> rejectedPane.getChildren().add(imgView);
                        case "approved" -> approvedPane.getChildren().add(imgView);
                        default -> pendingPane.getChildren().add(imgView);
                    }
                }

                if (!rejectedPane.getChildren().isEmpty()) {
                    Label rejectedLabel = new Label("❌ Rejected - Re-take photo");
                    rejectedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
                    photoSections.getChildren().addAll(rejectedLabel, rejectedPane);
                }

                if (!pendingPane.getChildren().isEmpty()) {
                    Label pendingLabel = new Label("⌛ Pending");
                    pendingLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: orange;");
                    photoSections.getChildren().addAll(pendingLabel, pendingPane);
                }

                if (!approvedPane.getChildren().isEmpty()) {
                    Label approvedLabel = new Label("✅ Approved");
                    approvedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
                    photoSections.getChildren().addAll(approvedLabel, approvedPane);
                }

                Button addPhotoButton = new Button("Add Photo");
                addPhotoButton.setOnAction(event -> openCameraWindow(item, approvedPane, order.getID(), order.getOrderNumber()));

                itemBox.getChildren().addAll(itemNameLabel, photoSections, addPhotoButton);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }


    private void openCameraWindow(Item item, FlowPane photoPane, int orderId, String orderNumber) {
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.open();

                ImageView liveView = new ImageView();
                liveView.setFitWidth(400);
                liveView.setFitHeight(300);
                liveView.setPreserveRatio(true);

                Thread webcamStream = new Thread(() -> {
                    while (webcam.isOpen()) {
                        BufferedImage frame = webcam.getImage();
                        if (frame != null) {
                            Image fxImage = SwingFXUtils.toFXImage(frame, null);
                            Platform.runLater(() -> liveView.setImage(fxImage));
                        }
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                webcamStream.setDaemon(true);
                webcamStream.start();

                Button takePhotoBtn = new Button("Take Photo");
                takePhotoBtn.setOnAction(e -> {
                    BufferedImage capturedFrame = webcam.getImage();
                    if (capturedFrame != null) {
                        Image capturedFxImage = SwingFXUtils.toFXImage(capturedFrame, null);
                        ImageView capturedImageView = new ImageView(capturedFxImage);
                        capturedImageView.setFitWidth(150);
                        capturedImageView.setFitHeight(150);
                        capturedImageView.setPreserveRatio(true);

                        Platform.runLater(() -> {
                            photoPane.getChildren().add(capturedImageView);
                            saveImageToDatabase(orderId, item.getId(), capturedFrame);
                        });
                    }
                    webcam.close();
                    ((Stage) takePhotoBtn.getScene().getWindow()).close();
                });

                VBox layout = new VBox(10, liveView, takePhotoBtn);
                layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

                Stage cameraStage = new Stage();
                cameraStage.setTitle(orderNumber + "-" + item.getItemName());
                cameraStage.setScene(new Scene(layout));
                cameraStage.show();

                cameraStage.setOnCloseRequest(e -> webcam.close());
            } else {
                System.out.println("No webcam detected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImageToDatabase(int orderId, int itemId, BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            orderManager.saveImage(orderId, itemId, imageBytes, 0); // Index auto-generated on backend
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
