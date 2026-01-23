package com.scm.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scm.services.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender eMailSender;

    @Value("${BREVO_FROM_EMAIL:noreply@yourdomain.com}")
    private String fromEmail;

    @Value("${BREVO_FROM_NAME:Smart Contact Manager}")
    private String fromName;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            System.out.println("\n========== EMAIL SENDING DEBUG ==========");
            System.out.println("To: " + to);
            System.out.println("From: " + fromEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body length: " + body.length());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(fromEmail);
            message.setSubject(subject);
            message.setText(body);
            
            System.out.println("Attempting to send email...");
            eMailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
            System.out.println("========================================\n");
        } catch (Exception e) {
            System.err.println("\n========== EMAIL ERROR ==========");
            System.err.println("❌ Failed to send email to " + to);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("=================================\n");
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailWithHtml() {
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithHtml'");
    }

    @Override
    public void sendEmailWithAttachment() {
        throw new UnsupportedOperationException("Unimplemented method 'sendEmailWithAttachment'");
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment) {
        try {
            MimeMessage mimeMessage = eMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setFrom(fromEmail, fromName);
            helper.setSubject(subject);
            helper.setText(body);
            
            if (attachment != null && !attachment.isEmpty()) {
                helper.addAttachment(attachment.getOriginalFilename(), attachment);
            }
            
            eMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

}
