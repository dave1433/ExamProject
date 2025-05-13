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

                    if ("rejected".equalsIgnoreCase(meta.getStatus())) {
                        StackPane stack = new StackPane();
                        stack.setPrefSize(150, 150);
                        stack.getChildren().add(imgView);

                        Button retakeBtn = new Button();
                        retakeBtn.setPrefSize(50, 50);
                        retakeBtn.setStyle("-fx-background-image: url('/dk/easv/blsgn/intgrpbelsign/Pictures/icons/icons8-retake-50.png'); " +
                                "-fx-background-color: transparent;");

                        StackPane overlay = new StackPane(retakeBtn);
                        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
                        overlay.setOpacity(0);
                        stack.getChildren().add(overlay);

                        stack.setOnMouseEntered(e -> overlay.setOpacity(1));
                        stack.setOnMouseExited(e -> overlay.setOpacity(0));

                        retakeBtn.setOnAction(e -> {
                            orderManager.deleteImage(meta.getId());
                            openRetakeCamera(item, order.getID(), order.getOrderNumber(), meta.getIndex());
                        });

                        rejectedPane.getChildren().add(stack);
                    } else if ("approved".equalsIgnoreCase(meta.getStatus())) {
                        approvedPane.getChildren().add(imgView);
                    } else {
                        StackPane stack = new StackPane();
                        stack.setPrefSize(150, 150);
                        stack.getChildren().add(imgView);

                        Button retakeBtn = new Button();
                        retakeBtn.setPrefSize(50, 50);
                        retakeBtn.setStyle("-fx-background-image: url('/dk/easv/blsgn/intgrpbelsign/Pictures/icons/icons8-retake-50.png'); " +
                                "-fx-background-color: transparent;");

                        StackPane overlay = new StackPane(retakeBtn);
                        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
                        overlay.setOpacity(0);
                        stack.getChildren().add(overlay);

                        stack.setOnMouseEntered(e -> overlay.setOpacity(1));
                        stack.setOnMouseExited(e -> overlay.setOpacity(0));

                        retakeBtn.setOnAction(e -> {
                            orderManager.deleteImage(meta.getId());
                            openRetakeCamera(item, order.getID(), order.getOrderNumber(), meta.getIndex());
                        });

                        pendingPane.getChildren().add(stack);
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
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
            ImageView liveView = new ImageView();
            liveView.setFitWidth(400);
            liveView.setFitHeight(300);
            liveView.setPreserveRatio(true);

            Thread stream = new Thread(() -> {
                while (webcam.isOpen()) {
                    BufferedImage frame = webcam.getImage();
                    if (frame != null) {
                        Image fxImage = SwingFXUtils.toFXImage(frame, null);
                        Platform.runLater(() -> liveView.setImage(fxImage));
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ignored) {}
                }
            });
            stream.setDaemon(true);
            stream.start();

            Button captureBtn = new Button("Capture");
            captureBtn.setOnAction(_ -> {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    saveImageToDatabase(orderId, item.getId(), frame, 0); // auto index
                    onSearchFilter();
                }
                webcam.close();
                ((Stage) captureBtn.getScene().getWindow()).close();
            });

            VBox layout = new VBox(10, liveView, captureBtn);
            layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

            Stage stage = new Stage();
            stage.setTitle("Capture - " + item.getItemName());
            stage.setScene(new Scene(layout));
            stage.setOnCloseRequest(_ -> webcam.close());
            stage.show();
        }
    }

    private void openRetakeCamera(Item item, int orderId, String orderNumber, int replaceIndex) {
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
            ImageView liveView = new ImageView();
            liveView.setFitWidth(400);
            liveView.setFitHeight(300);
            liveView.setPreserveRatio(true);

            Thread stream = new Thread(() -> {
                while (webcam.isOpen()) {
                    BufferedImage frame = webcam.getImage();
                    if (frame != null) {
                        Image fxImage = SwingFXUtils.toFXImage(frame, null);
                        Platform.runLater(() -> liveView.setImage(fxImage));
                    }
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException ignored) {}
                }
            });
            stream.setDaemon(true);
            stream.start();

            Button captureBtn = new Button("Retake Photo");
            captureBtn.setOnAction(_ -> {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(frame, "png", baos);
                        byte[] imageBytes = baos.toByteArray();
                        orderManager.saveImage(orderId, item.getId(), imageBytes, replaceIndex);
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }
                }
                webcam.close();
                ((Stage) captureBtn.getScene().getWindow()).close();
                onSearchFilter();
            });

            VBox layout = new VBox(10, liveView, captureBtn);
            layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

            Stage stage = new Stage();
            stage.setTitle("Retake - " + item.getItemName());
            stage.setScene(new Scene(layout));
            stage.setOnCloseRequest(_ -> webcam.close());
            stage.show();
        }
    }

    private void saveImageToDatabase(int orderId, int itemId, BufferedImage image, int imageIndex) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            orderManager.saveImage(orderId, itemId, imageBytes, imageIndex);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
