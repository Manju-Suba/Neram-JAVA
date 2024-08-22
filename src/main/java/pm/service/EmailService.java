package pm.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendEmail(String to, String subject, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setFrom(sender);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

//    public void sendEmailWithAttachment(String toEmail, String subject, String text, byte[] pdfBytes) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
////            MimeMessageHelper helper = new MimeMessageHelper(message, true);
////            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
////                    StandardCharsets.UTF_8.name());
//            MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//            helper.setTo(toEmail);
//            helper.setFrom(sender);
//            helper.setSubject(subject);
//            helper.setText(text,true);
//            helper.addAttachment("Daily Supervisor Report.pdf", new ByteArrayResource(pdfBytes));
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//    }
public void sendEmailWithAttachments(String recipientEmail, String subject, String text, Map<String, byte[]> attachments) {
    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.setFrom(sender);

        // Add attachments
        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            String attachmentName = entry.getKey();
            byte[] attachmentData = entry.getValue();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(attachmentData, "application/pdf");
            helper.addAttachment(attachmentName, dataSource);
        }

        mailSender.send(message);
    } catch (MessagingException e) {
        e.printStackTrace();
        // Log or handle the exception
    }
}


}
