package dk.easv.blsgn.intgrpbelsign.utils;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import dk.easv.blsgn.intgrpbelsign.be.Item;

public class PdfReportGenerator {

    public static byte[] generatePdfWithImages(String orderNumber, Item item, List<byte[]> imageList) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("Order Report").setBold().setFontSize(18));
        doc.add(new Paragraph("Order Number: " + orderNumber));
        doc.add(new Paragraph("Date: " + java.time.LocalDate.now()));
        doc.add(new Paragraph("\n"));

        addItemDetailsTable(doc, item);

        for (int i = 0; i < imageList.size(); i++) {
            byte[] imgBytes = imageList.get(i);
            if (imgBytes != null) {
                ImageData imageData = ImageDataFactory.create(imgBytes);
                Image image = new Image(imageData).scaleToFit(400, 300);
                doc.add(new Paragraph("Image " + (i + 1)));
                doc.add(image);
            }
        }

        doc.close();
        return baos.toByteArray();
    }

    private static void addItemDetailsTable(Document doc, Item item) {
        float[] columnWidths = {200f, 200f, 200f, 100f};
        Table table = new Table(columnWidths);

        table.addHeaderCell("Item Name");
        table.addHeaderCell("Materials Used");
        table.addHeaderCell("Approx. Quantity");
        table.addHeaderCell("Total Weight (g)");

        table.addCell(item.getItemName());
        table.addCell(item.getMaterialsUsed() != null ? item.getMaterialsUsed() : "N/A");
        table.addCell(item.getApproxQuantity() != null ? item.getApproxQuantity() : "N/A");
        table.addCell(String.valueOf(item.getTotalWeight()));

        doc.add(new Paragraph("Item Description:").setBold().setFontSize(14));
        doc.add(table);
        doc.add(new Paragraph("\n"));
    }
}
