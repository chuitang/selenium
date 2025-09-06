package com.example.smsreminder.sms;

public interface SmsService {
    void sendSms(String phoneNumber, String message);
}

