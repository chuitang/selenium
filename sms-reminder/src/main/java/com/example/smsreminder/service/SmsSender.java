package com.example.smsreminder.service;

public interface SmsSender {
    void send(String phone, String message);
}

