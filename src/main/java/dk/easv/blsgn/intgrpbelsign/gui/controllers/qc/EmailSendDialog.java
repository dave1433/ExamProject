package dk.easv.blsgn.intgrpbelsign.gui.controllers.qc;

import dk.easv.blsgn.intgrpbelsign.utils.EmailSender;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EmailSendDialog extends Stage {

    public EmailSendDialog(byte[] pdfBytes, String orderNumber) {
        setTitle("Send QC Documentation");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        Label label = new Label("Enter customer email:");
        TextField emailField = new TextField();
        emailField.setPromptText("example@domain.com");

        Button sendBtn = new Button("Send Email");

        sendBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (email == null || !email.contains("@")) {
                showAlert("Invalid email address.");
                return;
            }

            try {
                EmailSender.sendEmailWithAttachment(
                        emailField.getText(),
                        "Belman QC Documentation - Order " + orderNumber,
                        "Dear customer,\n\nPlease find attached the QC documentation for your order.\n\nRegards,\nBelman QC Team",
                        pdfBytes,
                        "Belman_Order_" + orderNumber + ".pdf"
                );
                showAlert("Email sent to " + email);
                close();
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Failed to send email.");
            }
        });

        layout.getChildren().addAll(label, emailField, sendBtn);
        setScene(new Scene(layout, 350, 150));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}
