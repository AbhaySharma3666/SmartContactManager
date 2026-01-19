package com.scm.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.scm.services.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsServiceImpl implements SmsService {

    private Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String fromPhoneNumber;

    @Override
    public boolean sendSms(String phoneNumber, String messageText) {
        try {
            logger.info("Attempting to send SMS to: {}", phoneNumber);
            logger.info("Twilio Account SID configured: {}", !accountSid.isEmpty());
            logger.info("Twilio Auth Token configured: {}", !authToken.isEmpty());
            logger.info("Twilio Phone Number configured: {}", fromPhoneNumber);
            
            // Check if Twilio is configured
            if (accountSid.isEmpty() || authToken.isEmpty() || fromPhoneNumber.isEmpty()) {
                logger.warn("Twilio not configured. SMS not sent. Message: {}", messageText);
                return false;
            }

            // Validate phone number format
            if (!phoneNumber.startsWith("+")) {
                logger.error("Phone number must be in international format starting with +");
                return false;
            }

            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized successfully");
            
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromPhoneNumber),
                    messageText)
                .create();

            logger.info("SMS sent successfully to {}. SID: {}, Status: {}", 
                phoneNumber, message.getSid(), message.getStatus());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
}
