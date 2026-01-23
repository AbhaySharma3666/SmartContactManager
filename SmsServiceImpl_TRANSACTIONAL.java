package com.scm.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.scm.services.SmsService;

@Service
public class SmsServiceImpl implements SmsService {

    private Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twofactor.api.key:}")
    private String apiKey;

    // 2Factor.in Transactional SMS API - 100% SMS only, NEVER voice
    private static final String TRANSACTIONAL_SMS_URL = "https://2factor.in/API/V1/{apiKey}/ADDON_SERVICES/SEND/TSMS";

    @Override
    public boolean sendSms(String phoneNumber, String messageText) {
        try {
            logger.info("Attempting to send SMS OTP to: {}", phoneNumber);
            logger.info("API Key configured: {}", (apiKey != null && !apiKey.isEmpty()) ? "YES" : "NO");
            
            if (apiKey == null || apiKey.isEmpty()) {
                logger.error("2Factor API key not configured. SMS not sent.");
                return false;
            }

            String otp = extractOTP(messageText);
            if (otp == null) {
                logger.error("Could not extract OTP from message");
                return false;
            }

            // Clean phone number - remove all non-digits and get last 10 digits
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }

            logger.info("Sending SMS OTP: {} to phone: {}", otp, cleanPhone);

            // Build SMS message
            String smsMessage = "Your OTP for Smart Contact Manager is " + otp + ". Valid for 5 minutes. Do not share with anyone.";
            
            RestTemplate restTemplate = new RestTemplate();
            String url = TRANSACTIONAL_SMS_URL.replace("{apiKey}", apiKey)
                + "?From=SCMAPP"
                + "&To=" + cleanPhone
                + "&Msg=" + URLEncoder.encode(smsMessage, StandardCharsets.UTF_8);
            
            logger.info("Calling 2Factor.in Transactional SMS API (SMS ONLY - NO VOICE)");

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            logger.info("2Factor API Response Status: {}", response.getStatusCode());
            logger.info("2Factor API Response Body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("SMS OTP sent successfully to {}", cleanPhone);
                return true;
            } else {
                logger.error("Failed to send SMS. Status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    private String extractOTP(String message) {
        // Extract OTP from message like "Your OTP for Smart Contact Manager is: 123456 Valid for 5 minutes."
        String[] parts = message.split(":");
        if (parts.length >= 2) {
            String otpPart = parts[1].trim().split(" ")[0];
            if (otpPart.matches("\\d{6}")) {
                return otpPart;
            }
        }
        return null;
    }
}
