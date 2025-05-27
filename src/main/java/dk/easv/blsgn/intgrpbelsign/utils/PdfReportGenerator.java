package dk.easv.blsgn.intgrpbelsign.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import dk.easv.blsgn.intgrpbelsign.be.ImageWithMeta;
import dk.easv.blsgn.intgrpbelsign.be.Item;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PdfReportGenerator {

    public static byte[] generatePdfWithImages(
            String orderNumber,
            Map<Item, List<ImageWithMeta>> itemImages,
            byte[] belmanLogoBytes,
            byte[] qcSignatureBytes,
            String qcUserName
    ) throws IOException {

        if (itemImages == null || itemImages.isEmpty()) {
            throw new IllegalArgumentException("PDF cannot be generated: No approved images.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        // Add Belman logo if available
        if (belmanLogoBytes != null) {
            ImageData logoData = ImageDataFactory.create(belmanLogoBytes);
            Image logo = new Image(logoData).scaleToFit(120, 60);
            doc.add(logo);
        }

        // Header
        doc.add(new Paragraph("Order Report").setBold().setFontSize(18));
        doc.add(new Paragraph("Order Number: " + orderNumber));
        doc.add(new Paragraph("Date: " + java.time.LocalDate.now()));
        doc.add(new Paragraph("\n"));

        // Grouped by item
        for (Map.Entry<Item, List<ImageWithMeta>> entry : itemImages.entrySet()) {
            Item item = entry.getKey();
            List<ImageWithMeta> images = entry.getValue();

            doc.add(new Paragraph("Item: " + item.getItemName()).setBold().setFontSize(14));
            doc.add(new Paragraph("\n"));

            for (ImageWithMeta img : images) {
                String angle = img.getViewType() != null ? img.getViewType() : "Unknown View";

                byte[] imgBytes = img.getImageData();
                if (imgBytes != null) {
                    ImageData imageData = ImageDataFactory.create(imgBytes);
                    Image image = new Image(imageData).scaleToFit(400, 300);
                    doc.add(new Paragraph(angle));
                    doc.add(image);
                }
            }

            doc.add(new Paragraph("\n"));
        }

        // QC approval
        if (qcUserName != null && qcSignatureBytes != null) {
            doc.add(new Paragraph("Approved by: " + qcUserName).setBold());
            ImageData signatureData = ImageDataFactory.create(qcSignatureBytes);
            Image signature = new Image(signatureData).scaleToFit(200, 100);
            doc.add(signature);
        }

        doc.close();
        return baos.toByteArray();
    }
}
