package dk.easv.blsgn.intgrpbelsign.utils;

import com.github.sarxos.webcam.Webcam;
import dk.easv.blsgn.intgrpbelsign.be.Item;
import dk.easv.blsgn.intgrpbelsign.bll.OrderManager;
import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class ImageOverlayUtil {
    private final OrderManager orderManager;
    private Runnable refreshCallback;

    public ImageOverlayUtil(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }


    public StackPane createImageWithOverlay(ImageView imgView, Image img, String status,
                                            ImageWithMeta meta, Item item, int orderId, String orderNumber) {
        StackPane stack = new StackPane();
        stack.setPrefSize(150, 150);
        stack.getChildren().add(imgView);

        VBox buttonsBox = new VBox(5);
        buttonsBox.setAlignment(Pos.CENTER);

        Button viewBtn = new Button();
        viewBtn.setPrefSize(50, 50);
        viewBtn.setStyle("-fx-background-image: url('/dk/easv/blsgn/intgrpbelsign/Pictures/icons/icons8-eye-50.png');" +
                "-fx-background-color: transparent;");
        viewBtn.setOnAction(_ -> openImageViewer(img));
        buttonsBox.getChildren().add(viewBtn);

        if (!"approved".equalsIgnoreCase(status)) {
            Button retakeBtn = new Button();
            retakeBtn.setPrefSize(50, 50);
            retakeBtn.setStyle("-fx-background-image: url('/dk/easv/blsgn/intgrpbelsign/Pictures/icons/icons8-retake-50.png');" +
                    "-fx-background-color: transparent;");
            retakeBtn.setOnAction(_ -> {
                orderManager.deleteImage(meta.getId());
                openRetakeCamera(item, orderId, orderNumber);
            });
            buttonsBox.getChildren().add(retakeBtn);
        }

        StackPane overlay = new StackPane(buttonsBox);
        overlay.setStyle("-fx-background-color: rgba(255, 255, 255,0.5);");
        overlay.setOpacity(0);
        stack.getChildren().add(overlay);

        stack.setOnMouseEntered(_ -> overlay.setOpacity(1));
        stack.setOnMouseExited(_ -> overlay.setOpacity(0));

        return stack;
    }

    public void openCameraWindow(Item item, int orderId, String orderNumber) {
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
            ImageView liveView = new ImageView();
            liveView.setFitWidth(400);
            liveView.setFitHeight(300);
            liveView.setPreserveRatio(true);

            Thread stream = getStream(webcam, liveView);
            stream.start();

            Button captureBtn = new Button("Take Photo");
            captureBtn.setOnAction(_ -> {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    saveImageToDatabase(orderId, item.getId(), frame, 0);
                    if (refreshCallback != null) {
                        Platform.runLater(refreshCallback);
                    }
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
        } else {
            showError("Camera Error", "No webcam detected");
        }
    }

    public void openRetakeCamera(Item item, int orderId, String orderNumber) {
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
            ImageView liveView = new ImageView();
            liveView.setFitWidth(400);
            liveView.setFitHeight(300);
            liveView.setPreserveRatio(true);

            Thread stream = getStream(webcam, liveView);
            stream.start();

            Button captureBtn = new Button("Retake Photo");
            captureBtn.setOnAction(_ -> {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(frame, "png", baos);
                        byte[] imageBytes = baos.toByteArray();
                        orderManager.saveImage(orderId, item.getId(), imageBytes);
                        if (refreshCallback != null) {
                            Platform.runLater(refreshCallback);
                        }
                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                        showError("Error", "Failed to save image: " + e.getMessage());
                    }
                }
                webcam.close();
                ((Stage) captureBtn.getScene().getWindow()).close();
            });


            VBox layout = new VBox(10, liveView, captureBtn);
            layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

            Stage stage = new Stage();
            stage.setTitle("Retake - " + item.getItemName());
            stage.setScene(new Scene(layout));
            stage.setOnCloseRequest(_ -> webcam.close());
            stage.show();
        } else {
            showError("Camera Error", "No webcam detected");
        }
    }

    private void saveImageToDatabase(int orderId, int itemId, BufferedImage image, int i) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            orderManager.saveImage(orderId, itemId, imageBytes);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to save image: " + e.getMessage());
        }
    }

    private void openImageViewer(Image img) {
        Stage viewStage = new Stage();
        ImageView largeView = new ImageView(img);
        largeView.setPreserveRatio(true);
        largeView.setFitWidth(800);
        largeView.setFitHeight(600);

        VBox layout = new VBox(largeView);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center; -fx-background-color: black;");

        Scene scene = new Scene(layout);
        viewStage.setScene(scene);
        viewStage.setTitle("Image Viewer");
        viewStage.show();
    }

    private Thread getStream(Webcam webcam, ImageView liveView) {
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
        return stream;
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}