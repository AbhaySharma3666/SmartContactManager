package com.scm.services;

public interface SmsService {
    boolean sendSms(String phoneNumber, String message);
}
