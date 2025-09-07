package com.example.smsreminder.service;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.repository.ReminderTaskRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TaskSchedulerService {

    private final ReminderTaskRepository repository;
    private final SmsSender smsSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public TaskSchedulerService(ReminderTaskRepository repository, SmsSender smsSender) {
        this.repository = repository;
        this.smsSender = smsSender;
    }

    // run every minute
    @Scheduled(cron = "0 * * * * *")
    public void checkAndSendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<ReminderTask> tasks = repository.findActiveEffective(now);
        for (ReminderTask task : tasks) {
            if (!shouldSend(now, task)) {
                continue;
            }
            String link = baseUrl + "/complete?taskId=" + task.getId();
            String message = task.getContent() + " | 点击确认完成: " + link;
            smsSender.send(task.getPhone(), message);
            task.setLastSentAt(now);
            repository.save(task);
        }
    }

    boolean shouldSend(LocalDateTime now, ReminderTask task) {
        // if already completed this period (today after first reminder time), do not send
        LocalDateTime todaysFirstReminder = LocalDateTime.of(LocalDate.now(), LocalTime.of(task.getDailyHour(), task.getDailyMinute()));
        if (now.isBefore(todaysFirstReminder)) {
            return false;
        }
        if (task.getLastCompletedAt() != null && !task.getLastCompletedAt().isBefore(todaysFirstReminder)) {
            return false;
        }
        // if never sent today, send when now >= first time
        if (task.getLastSentAt() == null || task.getLastSentAt().isBefore(todaysFirstReminder)) {
            return now.isAfter(todaysFirstReminder) || now.isEqual(todaysFirstReminder);
        }
        // otherwise enforce interval minutes
        long minutesSinceLast = ChronoUnit.MINUTES.between(task.getLastSentAt(), now);
        return minutesSinceLast >= task.getIntervalMinutes();
    }
}

