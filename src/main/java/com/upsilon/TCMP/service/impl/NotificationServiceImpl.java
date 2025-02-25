package com.upsilon.TCMP.service.impl;

import com.upsilon.TCMP.entity.Message;
import com.upsilon.TCMP.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private SpringTemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void notifyNewMessage(Message message) {
        String subject = "New Message Received";
        String content = "You have received a new message from " + message.getSender().getFullName();
        sendEmail(message.getReceiver().getEmail(), subject, content);
    }

    @Override
    public void sendVerificationNotification(Integer userId) {
        // This method is not used directly, we use sendEmailWithTemplate instead
    }

    @Override
    public void sendEmailWithTemplate(String email, String templateName, Map<String, String> params) {
        try {
            logger.info("Starting to send email template '{}' to {}", templateName, email);
            
            // Create a Thymeleaf context and process the template
            Context context = new Context();
            String token = params.get("token");
            String subject;

            // Set up template-specific variables and subject
            if ("email_verification".equals(templateName)) {
                String verificationLink = String.format("http://localhost:8080/api/verify-email?token=%s", token);
                context.setVariable("verificationLink", verificationLink);
                subject = "Verify Your Email Address - TCMP";
                logger.debug("Created verification link: {}", verificationLink);
            } else if ("password_reset".equals(templateName)) {
                String resetLink = String.format("http://localhost:8080/api/reset-password?token=%s", token);
                context.setVariable("resetLink", resetLink);
                subject = "Reset Your Password - TCMP";
            } else {
                subject = "TCMP Notification";
            }

            // Set all parameters in context
            for (Map.Entry<String, String> entry : params.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }

            logger.debug("Processing template '{}'", templateName);
            String htmlContent = templateEngine.process("email/" + templateName, context);
            logger.debug("Template processed successfully");

            // Create and send email
            logger.debug("Creating email message");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            logger.info("Attempting to send email to {} with subject: {}", email, subject);
            mailSender.send(mimeMessage);
            logger.info("Email sent successfully to {}", email);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}