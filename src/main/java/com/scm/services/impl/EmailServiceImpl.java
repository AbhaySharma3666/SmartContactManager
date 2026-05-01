package com.scm.services.impl;

import com.scm.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body
            );
            send(payload);
            logger.info("Email sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, MultipartFile attachment) {
        try {
            String encodedFile = Base64.getEncoder().encodeToString(attachment.getBytes());
            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", fromName, "email", fromEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body,
                    "attachment", List.of(Map.of(
                            "name", attachment.getOriginalFilename(),
                            "content", encodedFile
                    ))
            );
            send(payload);
            logger.info("Email with attachment sent to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email with attachment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email with attachment: " + e.getMessage(), e);
        }
    }

    private void send(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        restTemplate.postForEntity(BREVO_API_URL, new HttpEntity<>(payload, headers), String.class);
    }

    @Override public void sendEmailWithHtml() { throw new UnsupportedOperationException(); }
    @Override public void sendEmailWithAttachment() { throw new UnsupportedOperationException(); }
}
