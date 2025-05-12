package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

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
        onSearchFilter();
    }

    @FXML
    private void onSearchFilter() {
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

                FlowPane photoPane = new FlowPane(5, 5);
                photoPane.setPrefWrapLength(600);

                List<byte[]> images = orderManager.getImagesForItem(order.getID(), item.getId());
                for (byte[] imgBytes : images) {
                    Image img = new Image(new ByteArrayInputStream(imgBytes));
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(150);
                    imgView.setFitHeight(150);
                    imgView.setPreserveRatio(true);
                    photoPane.getChildren().add(imgView);
                }

                // Create buttons
                Button approvedBtn = new Button("Approved");
                Button rejectedBtn = new Button("Rejected");

                approvedBtn.setStyle("-fx-background-color: lightgreen; -fx-font-weight: bold;");
                rejectedBtn.setStyle("-fx-background-color: lightcoral; -fx-font-weight: bold;");

                approvedBtn.setOnAction(e -> {
                    System.out.println("Approved item: " + item.getItemName() + " in Order: " + order.getOrderNumber());
                    // Add approval logic here
                });

                rejectedBtn.setOnAction(e -> {
                    System.out.println("Rejected item: " + item.getItemName() + " in Order: " + order.getOrderNumber());
                    // Add rejection logic here
                });

                HBox buttonBox = new HBox(10, approvedBtn, rejectedBtn);
                buttonBox.setAlignment(Pos.CENTER_LEFT);

                itemBox.getChildren().addAll(itemNameLabel, photoPane, buttonBox);
                itemsContainer.getChildren().add(itemBox);
            }

            orderBox.getChildren().addAll(orderLabel, itemsContainer);
            flowPane.getChildren().add(orderBox);
        }
    }

    /*private void openCameraWindow(Item item, FlowPane photoPane, int orderId) {
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
                        photoPane.getChildren().add(capturedImageView);
                        // Save logic can go here
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
                System.out.println("No webcam detected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
