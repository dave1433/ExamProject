package dk.easv.blsgn.intgrpbelsign.gui.controllers;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;

public class PdfPreviewDialog extends Stage {

    private final byte[] pdfBytes;
    private final String orderNumber;

    public PdfPreviewDialog(byte[] pdfBytes, String orderNumber) throws IOException {
        this.pdfBytes = pdfBytes;
        this.orderNumber = orderNumber;

        setTitle("PDF Preview");
        initModality(Modality.APPLICATION_MODAL);

        VBox imageContainer = new VBox(10);
        imageContainer.setStyle("-fx-padding: 10;");
        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Button downloadBtn = new Button("Download PDF");

        BorderPane root = new BorderPane(scrollPane);
        root.setBottom(downloadBtn);

        Scene scene = new Scene(root, 600, 700);
        setScene(scene);

        // Render all pages
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

        downloadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save PDF As...");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            chooser.setInitialFileName("Order_" + orderNumber + ".pdf");

            File file = chooser.showSaveDialog(this);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(pdfBytes);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to save PDF").showAndWait();
                }
            }
        });
    }
}
