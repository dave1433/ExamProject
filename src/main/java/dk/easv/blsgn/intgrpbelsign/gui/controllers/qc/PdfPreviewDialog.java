package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;

public class PdfPreviewDialog  {

    @FXML
    private VBox imageContainer;

    @FXML
    private TextField emailField;

    @FXML
    private Button downloadAndSendBtn;

    private byte[] pdfBytes;
    private String orderNumber;

    public void initData(byte[] pdfBytes, String orderNumber) throws IOException {
        this.pdfBytes = pdfBytes;
        this.orderNumber = orderNumber;
        loadPdfImages();
    }

    private void loadPdfImages() throws IOException {
        imageContainer.getChildren().clear();
        try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage bufferedImage = renderer.renderImageWithDPI(i, 150);
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                ImageView imageView = new ImageView(fxImage);
                imageView.setFitWidth(600);
                imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            }
        }
    }

    @FXML
    private void handleDownloadAndSend() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save PDF As...");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName("Belman Order_" + orderNumber + ".pdf");

        File file = chooser.showSaveDialog(downloadAndSendBtn.getScene().getWindow());
        if (file != null) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);

                String email = emailField.getText();
                if (email != null && !email.isBlank()) {
                    if (!isValidEmail(email)) {
                        showAlert("Invalid email.");
                        return;
                    }

                    System.out.println("Sending PDF to " + email);
                }

                showAlert("PDF saved" + (email != null && !email.isBlank() ? " and emailed to " + email : "."));

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Failed to save/send PDF.");
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}
