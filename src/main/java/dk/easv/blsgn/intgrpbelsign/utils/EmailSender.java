package dk.easv.blsgn.intgrpbelsign.utils;

import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.util.Base64;
import java.util.Properties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EmailSender {

    private static final String API_KEY = System.getenv("SENDGRID_API_KEY");
    private static final String FROM_EMAIL = "luipac01@easv365.dk"; // Must be a verified sender in SendGrid

    public static void sendEmailWithAttachment(
            String toEmail,
            String subject,
            String plainBody,
            byte[] pdfBytes,
            String fileName
    ) throws IOException {
        Email from = new Email(FROM_EMAIL);
        Email to = new Email(toEmail);

        // 1. Create mail object and add both plain + HTML content
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);
        mail.addContent(new Content("text/plain", plainBody));
        mail.addContent(new Content("text/html",
                "<p>Dear customer,</p>" +
                        "<p>Please find attached the QC documentation for your order.</p>" +
                        "<p>Best regards,<br><strong>Belman QC Team</strong></p>" +
                        "<hr><p style='font-size: small; color: gray;'>This is an automated message. Do not reply directly.</p>"
        ));

        mail.setReplyTo(new Email("qc@belman.com")); // Optional: set reply address

        // 2. Add recipient
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        mail.addPersonalization(personalization);

        // 3. Add PDF attachment
        Attachments attachment = new Attachments();
        attachment.setFilename(fileName);
        attachment.setType("application/pdf");
        attachment.setDisposition("attachment");
        attachment.setContent(Base64.getEncoder().encodeToString(pdfBytes));
        mail.addAttachments(attachment);

        // 4. Send request
        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("STATUS: " + response.getStatusCode());
            System.out.println("BODY: " + response.getBody());
        } catch (IOException ex) {
            throw ex;
        }
    }
}
