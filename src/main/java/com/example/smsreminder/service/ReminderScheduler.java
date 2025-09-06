package com.example.smsreminder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);
    private final ReminderService reminderService;

    public ReminderScheduler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    // Run every 1 minute
    @Scheduled(fixedDelayString = "${app.scheduler.fixedDelayMs:60000}")
    public void run() {
        log.debug("Running reminder evaluation...");
        reminderService.evaluateAndSendDueReminders();
    }
}

