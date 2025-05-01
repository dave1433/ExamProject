package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.application.Platform;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
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

    @FXML
    public void initialize() {
        displayOrd(orderManager.getAllOrders());
    }

    @FXML
    private void onSearch() {
        String input = searchField.getText().trim();

        if (!input.isEmpty()) {
            List<Order> matchingOrders = orderManager.getAllOrders()
                    .stream()
                    .filter(o -> o.getOrderNumber().equals(input))
                    .collect(Collectors.toList());

            if (!matchingOrders.isEmpty()) {
                displayOrders(matchingOrders);
            } else {
                flowPane.getChildren().clear();
                flowPane.getChildren().add(new Label("No orders found for number: " + input));
            }
        }
    }

    private void displayOrders(List<Order> orders) {
        flowPane.getChildren().clear();

        for (Order order : orders) {
            VBox orderBox = new VBox(10);
            orderBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
            orderBox.setPrefWidth(700);

            Label orderLabel = new Label("Order: " + order.getOrderNumber());
            orderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            VBox itemsContainer = new VBox(15); // This will hold item boxes

            for (Item item : order.getItems()) {
                VBox itemBox = new VBox(5);
                itemBox.setAlignment(Pos.TOP_LEFT);
                itemBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                itemBox.setPrefWidth(650);

                Label itemNameLabel = new Label(item.getItemName());
                itemNameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

                // FlowPane to hold captured images
                FlowPane photoPane = new FlowPane();
                photoPane.setHgap(5);
                photoPane.setVgap(5);
                photoPane.setPrefWrapLength(600); // Wrap images if many

                Button addPhotoButton = new Button("Add Photo");
                addPhotoButton.setOnAction(event -> openCameraWindow(item, photoPane));

                itemBox.getChildren().addAll(itemNameLabel, photoPane, addPhotoButton);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }

    private void displayOrd(List<Order> orders) {
        ObservableList<String> orderNumbers = FXCollections.observableArrayList();
        for (Order order : orders) {
            orderNumbers.add("Order: " + order.getOrderNumber());
        }
        listView.setItems(orderNumbers);
    }

    private void openCameraWindow(Item item, FlowPane photoPane) {
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
                            if (photoPane.getChildren().size() < 5) {
                                photoPane.getChildren().add(capturedImageView);
                            } else {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Maximum 5 photos allowed.");
                                alert.showAndWait();
                            }
                        });
                    }
                    webcam.close();
                    ((Stage) takePhotoBtn.getScene().getWindow()).close();
                });

                VBox layout = new VBox(10, liveView, takePhotoBtn);
                layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

                Stage cameraStage = new Stage();
                cameraStage.setTitle("Camera - Take Photo");
                cameraStage.setScene(new Scene(layout));
                cameraStage.show();

                cameraStage.setOnCloseRequest(e -> webcam.close());
            } else {
                System.out.println("No webcam detected");
            }
        } catch (WebcamException e) {
            e.printStackTrace();
        }
    }
}
