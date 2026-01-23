package com.scm.services.impl;

import com.scm.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${twofactor.api.key:}")
    private String apiKey;

    private static final String SMS_OTP_URL = "https://2factor.in/API/V1/{apiKey}/SMS/{phoneNumber}/{otp}";

    @Override
    public String sendOtp(String phoneNumber) {
        try {
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }
            
            String otp = String.format("%06d", random.nextInt(999999));
            otpStore.put(cleanPhone, otp);
            
            logger.info("Sending OTP to: {}", cleanPhone);
            
            if (apiKey != null && !apiKey.isEmpty()) {
                try {
                    String url = SMS_OTP_URL.replace("{apiKey}", apiKey)
                                            .replace("{phoneNumber}", cleanPhone)
                                            .replace("{otp}", otp);
                    
                    String response = restTemplate.getForObject(url, String.class);
                    logger.info("2Factor API Response: {}", response);
                    logger.info("SMS sent successfully to {}", cleanPhone);
                } catch (Exception e) {
                    logger.error("Failed to send SMS via 2Factor: {}", e.getMessage());
                }
            } else {
                logger.warn("2Factor API key not configured. OTP: {}", otp);
            }
            
            return otp;
            
        } catch (Exception e) {
            logger.error("Failed to send OTP: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean verifyOtp(String phoneNumber, String otp) {
        try {
            String cleanPhone = phoneNumber.replaceAll("[^0-9]", "");
            if (cleanPhone.length() > 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }
            
            String storedOtp = otpStore.get(cleanPhone);
            
            if (storedOtp != null && storedOtp.equals(otp)) {
                otpStore.remove(cleanPhone);
                logger.info("OTP verified successfully for {}", cleanPhone);
                return true;
            }
            
            logger.warn("Invalid OTP for {}", cleanPhone);
            return false;
            
        } catch (Exception e) {
            logger.error("OTP verification failed: {}", e.getMessage());
            return false;
        }
    }
}
