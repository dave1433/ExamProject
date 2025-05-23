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


    private Runnable refreshCallback;
    private final OrderManager orderManager;

    public ImageOverlayUtil(OrderManager orderManager) {
        this.orderManager = orderManager;
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }


    public StackPane createImageWithOverlay(ImageView imgView, Image img, String status, String viewType,
                                            ImageWithMeta meta, Item item, int orderId,
                                            String orderNumber, boolean isQCView) {

        StackPane container = new StackPane();
        container.setPrefSize(110, 110);

        // Ensure image sizing is consistent
        imgView.setFitWidth(110);
        imgView.setFitHeight(110);
        imgView.setPreserveRatio(true);
        imgView.setOnMouseClicked(_ -> openImageViewer(img));
        container.getChildren().add(imgView);

        // If the image is not approved, show a retake picture icon (unless we're in QC view)
        if (!isQCView && !"approved".equalsIgnoreCase(status)) {
            ImageView retakeIcon = new ImageView(new Image("/dk/easv/blsgn/intgrpbelsign/Pictures/icons/icons8-camera-50.png"));
            retakeIcon.setFitWidth(20);
            retakeIcon.setFitHeight(20);
            StackPane.setAlignment(retakeIcon, Pos.TOP_RIGHT);
            retakeIcon.setStyle("-fx-cursor: hand;");
            retakeIcon.setOnMouseClicked(e -> {
                orderManager.deleteImage(meta.getId());
                openRetakeCamera(item, orderId, orderNumber, viewType);
            });

            container.getChildren().add(retakeIcon);
        }

        return container;
    }

    public void openCameraWindow(Item item, int orderId, String orderNumber, String viewType) {
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
            saveImage(item, orderId, viewType, webcam, captureBtn);

            VBox layout = new VBox(10, liveView, captureBtn);
            layout.setStyle("-fx-padding: 10; -fx-alignment: center;");

            Stage stage = new Stage();
            stage.setTitle("Capture - " + item.getItemName() + " (" + viewType + ")");
            stage.setScene(new Scene(layout));
            stage.setOnCloseRequest(_ -> webcam.close());
            stage.show();
        } else {
            showError("Camera Error", "No webcam detected");
        }
    }

    private void saveImage(Item item, int orderId, String viewType, Webcam webcam, Button captureBtn) {
        captureBtn.setOnAction(_ -> {
            BufferedImage frame = webcam.getImage();
            if (frame != null) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(frame, "png", baos);
                    byte[] imageBytes = baos.toByteArray();
                    orderManager.saveImage(orderId, item.getId(), imageBytes, viewType);
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
    }


    public void openRetakeCamera(Item item, int orderId, String orderNumber, String viewType) {
        Webcam webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.open();
            ImageView liveView = new ImageView();
            liveView.setFitWidth(400);
            liveView.setFitHeight(300);
            liveView.setPreserveRatio(true);

            Thread stream = getStream(webcam, liveView);
            stream.start();

            var captureBtn = getRetakeBtn(item, orderId, webcam,viewType);


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

    private Button getRetakeBtn(Item item, int orderId, Webcam webcam, String viewType) {
        Button retakeBtn = new Button("Retake Photo");
        saveImage(item, orderId, viewType, webcam, retakeBtn);
        return retakeBtn;
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
                } catch (InterruptedException ignored) {
                }
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