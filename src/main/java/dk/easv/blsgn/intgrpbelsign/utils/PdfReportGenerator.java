package dk.easv.blsgn.intgrpbelsign.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import dk.easv.blsgn.intgrpbelsign.be.Item;

public class PdfReportGenerator {

    public static byte[] generatePdfWithImages(String orderNumber, List<byte[]> approvedImageList, byte[] belmanLogoBytes, byte[] qcSignatureBytes
    ) throws IOException {

        if (approvedImageList == null || approvedImageList.isEmpty()) {
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

        // Basic report info
        doc.add(new Paragraph("Order Report").setBold().setFontSize(18));
        doc.add(new Paragraph("Order Number: " + orderNumber));
        doc.add(new Paragraph("Date: " + java.time.LocalDate.now()));
        doc.add(new Paragraph("\n"));

        // Add approved images
        for (int i = 0; i < approvedImageList.size(); i++) {
            byte[] imgBytes = approvedImageList.get(i);
            if (imgBytes != null) {
                ImageData imageData = ImageDataFactory.create(imgBytes);
                Image image = new Image(imageData).scaleToFit(400, 300);
                doc.add(new Paragraph("Image " + (i + 1)));
                doc.add(image);
            }
        }

        // Add QC signature if available
        if (qcSignatureBytes != null) {
            doc.add(new Paragraph("\nApproved by the Quality Control Department").setBold());
            ImageData signatureData = ImageDataFactory.create(qcSignatureBytes);
            Image signature = new Image(signatureData).scaleToFit(200, 100);
            doc.add(signature);
        }

        doc.close();
        return baos.toByteArray();

    }
}


