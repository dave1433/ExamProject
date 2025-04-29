package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import dk.easv.blsgn.intgrpbelsign.be.Order;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
        // Optionally load all orders on start
        displayOrd(orderManager.getAllOrders());
    }

    @FXML
    private void onSearch() {
        String input = searchField.getText().trim();

        if (!input.isEmpty()) {
            try {
                int searchedItemNumber = Integer.parseInt(input);
                List<Order> matchingOrders = orderManager.getAllOrders()
                        .stream()
                        .filter(o -> o.getOrderNumber() == searchedItemNumber)
                        .collect(Collectors.toList());

                displayOrders(matchingOrders);
            } catch (NumberFormatException e) {
                // handle invalid input (non-integer)
                flowPane.getChildren().clear();
                flowPane.getChildren().add(new Label("Please enter a valid number."));
            }
        }
    }

    // Modified to use FlowPane for each order
    private void displayOrders(List<Order> orders) {
        flowPane.getChildren().clear(); // Clear the flowPane before adding the updated list

        for (Order order : orders) {
            // Create a new FlowPane for each order
            FlowPane orderFlowPane = new FlowPane();
            orderFlowPane.setHgap(10);
            orderFlowPane.setVgap(10);
            orderFlowPane.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1;");
            orderFlowPane.setPrefWidth(700); // Adjust width as needed

            // Create the item number label
            Label itemNumberLabel = new Label("Item: " + order.getItemNumber());

            // Load the icon image (adjust path based on your resources structure)
            Image icon = new Image(getClass().getResourceAsStream("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/camera.png"));
            javafx.scene.image.ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(100);
            iconView.setFitHeight(100);

            // Create a button with only the icon
            Button detailsButton = new Button();
            detailsButton.setGraphic(iconView);
            detailsButton.setPrefSize(150, 150);
            detailsButton.setMinSize(150, 150);
            detailsButton.setMaxSize(150, 150);
            detailsButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            // Optional: add a tooltip
            Tooltip.install(detailsButton, new Tooltip("View details"));

            // Add functionality to the details button
            detailsButton.setOnAction(event -> openCameraWindow(order, orderFlowPane));

            // Add the item number label and the button to the order's FlowPane
            orderFlowPane.getChildren().addAll(itemNumberLabel, detailsButton);

            // Add the order's FlowPane to the main flowPane
            flowPane.getChildren().add(orderFlowPane);
        }
    }

    private void displayOrd(List<Order> orders) {
        ObservableList<String> orderNumbers = FXCollections.observableArrayList();

        for (Order order : orders) {
            // Add each order number to the ListView
            orderNumbers.add("Order: " + order.getOrderNumber());
        }

        // Set the list of order numbers to the ListView
        listView.setItems(orderNumbers);
    }

    // Modified to accept the FlowPane for the specific order
    private void openCameraWindow(Order order, FlowPane orderFlowPane) {
        try {
            // Open the default webcam
            Webcam webcam = Webcam.getDefault();
            if (webcam != null) {
                System.out.println("Webcam opened successfully.");
                webcam.open();

                // Create an ImageView to show the live webcam feed
                ImageView liveView = new ImageView();
                liveView.setFitWidth(400);
                liveView.setFitHeight(300);
                liveView.setPreserveRatio(true);

                // Start a background thread to update the ImageView
                Thread webcamStream = new Thread(() -> {
                    while (webcam.isOpen()) {
                        BufferedImage frame = webcam.getImage();
                        if (frame != null) {
                            Image fxImage = SwingFXUtils.toFXImage(frame, null);
                            Platform.runLater(() -> liveView.setImage(fxImage));
                        }
                        try {
                            Thread.sleep(30); // ~30 FPS
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                webcamStream.setDaemon(true);
                webcamStream.start();

                // Create a "Take Photo" button
                Button takePhotoBtn = new Button("Take Photo");
                takePhotoBtn.setOnAction(e -> {
                    System.out.println("Take Photo button clicked.");
                    // Capture the current frame
                    BufferedImage capturedFrame = webcam.getImage();
                    if (capturedFrame != null) {
                        System.out.println("Frame captured successfully.");
                        Image capturedFxImage = SwingFXUtils.toFXImage(capturedFrame, null);

                        // Create an ImageView for the captured photo
                        ImageView capturedImageView = new ImageView(capturedFxImage);
                        capturedImageView.setFitWidth(200); // Increase width
                        capturedImageView.setFitHeight(200); // Increase height
                        capturedImageView.setPreserveRatio(true);
                        /*
                        // Add the captured image directly to the specific order's FlowPane
                       Platform.runLater(() -> orderFlowPane.getChildren().add(capturedImageView));
                        //orderFlowPane.getChildren().add(orderFlowPane.getChildren().size() - 1, capturedImageView);*/
                        Platform.runLater(() -> orderFlowPane.getChildren().add(orderFlowPane.getChildren().size() - 1, capturedImageView));

                    } else {
                        System.out.println("Failed to capture frame.");
                    }

                    // Close the webcam and the camera window
                    webcam.close();
                    ((Stage) takePhotoBtn.getScene().getWindow()).close();
                });

                // Layout for the camera window (VBox containing the live view and the button)
                VBox layout = new VBox(10, liveView, takePhotoBtn);
                layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

                // New stage (window) for the camera
                Stage cameraStage = new Stage();
                cameraStage.setTitle("Camera - Take Photo");
                cameraStage.setScene(new Scene(layout));
                cameraStage.show();

                // Close the webcam if the user closes the camera window manually
                cameraStage.setOnCloseRequest(e -> webcam.close());

            } else {
                System.out.println("No webcam detected");
            }
        } catch (WebcamException e) {
            e.printStackTrace();
        }
    }
}
