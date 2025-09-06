package com.example.smsreminder.service;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.repository.ReminderTaskRepository;
import com.example.smsreminder.sms.SmsService;
import com.example.smsreminder.util.HmacSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderService {

    private final ReminderTaskRepository repository;
    private final SmsService smsService;
    private final HmacSigner signer;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ReminderService(ReminderTaskRepository repository,
                           SmsService smsService,
                           @Value("${app.hmac-secret:dev-secret}") String hmacSecret) {
        this.repository = repository;
        this.smsService = smsService;
        this.signer = new HmacSigner(hmacSecret);
    }

    public void evaluateAndSendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<ReminderTask> tasks = repository.findAll();
        for (ReminderTask task : tasks) {
            if (task.shouldNotify(now)) {
                sendReminder(task, now);
            }
        }
    }

    public void sendReminder(ReminderTask task, LocalDateTime now) {
        String completionLink = buildCompletionLink(task);
        String message = String.format("%s\n确认完成: %s", task.getContent(), completionLink);
        smsService.sendSms(task.getPhoneNumber(), message);
        task.setLastNotifiedAt(now);
        repository.save(task);
    }

    private String buildCompletionLink(ReminderTask task) {
        String tokenData = task.getId() + ":" + LocalDate.now();
        String token = signer.sign(tokenData);
        return baseUrl + "/tasks/" + task.getId() + "/complete?d=" + LocalDate.now() + "&t=" + token;
    }

    public boolean completeTask(Long taskId, LocalDate date, String token) {
        ReminderTask task = repository.findById(taskId).orElse(null);
        if (task == null) return false;
        String expectedData = task.getId() + ":" + date;
        if (!signer.verify(expectedData, token)) return false;
        task.setLastCompletedDate(date);
        repository.save(task);
        return true;
    }
}

