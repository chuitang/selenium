package com.example.smsreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmsReminderApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmsReminderApplication.class, args);
    }
}

