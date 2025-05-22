package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import javafx.embed.swing.SwingFXUtils;
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

public class PdfPreviewDialog extends Stage {

    public PdfPreviewDialog(byte[] pdfBytes, String orderNumber) throws IOException {
        setTitle("PDF Preview");
        initModality(Modality.APPLICATION_MODAL);

        VBox imageContainer = new VBox(10);
        imageContainer.setStyle("-fx-padding: 10;");
        ScrollPane scrollPane = new ScrollPane(imageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        Button downloadBtn = new Button("Download PDF");
        downloadBtn.setStyle("-fx-background-color:  #004b88; -fx-text-fill: white; -fx-padding: 8;");

        BorderPane root = new BorderPane(scrollPane);
        HBox downloadBtnHbox = new HBox(downloadBtn);
        downloadBtnHbox.setAlignment(Pos.CENTER);
        downloadBtnHbox.setPadding(new Insets(10));
        root.setBottom(downloadBtnHbox);

        Scene scene = new Scene(root, 600, 700);
        setScene(scene);

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
            chooser.setInitialFileName("Belman Order_" + orderNumber + ".pdf");

            File file = chooser.showSaveDialog(this);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(pdfBytes);

                    // ✅ Ask for email and pass reference to this window
                    new EmailSendDialog(pdfBytes, orderNumber, this).show();

                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to save PDF").showAndWait();
                }
            }
        });
    }
}
