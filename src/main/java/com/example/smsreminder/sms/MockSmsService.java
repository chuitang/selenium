package com.example.smsreminder.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(SmsService.class)
public class MockSmsService implements SmsService {
    private static final Logger log = LoggerFactory.getLogger(MockSmsService.class);

    @Override
    public void sendSms(String phoneNumber, String message) {
        log.info("[MOCK SMS] to={} message={}", phoneNumber, message);
    }
}

