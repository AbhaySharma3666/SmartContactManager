package com.scm.services;

public interface SmsService {
    String sendOtp(String phoneNumber);
    boolean verifyOtp(String phoneNumber, String otp);
}
