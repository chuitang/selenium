package com.example.smsreminder.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminder_tasks")
public class ReminderTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    // Cron-like or fixed interval minutes
    // For simplicity, we use two parts: dailyStartTime (like 19:00) and intervalMinutes for re-reminders
    @NotNull
    private Integer intervalMinutes; // re-reminder interval when pending

    @NotNull
    @Min(0)
    @Max(23)
    private Integer dailyHour; // first reminder hour of day

    @NotNull
    @Min(0)
    @Max(59)
    private Integer dailyMinute; // first reminder minute of day

    @NotBlank
    @Column(length = 500)
    private String content;

    @NotBlank
    private String phone;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskStatus status = TaskStatus.ACTIVE;

    @NotNull
    private LocalDateTime effectiveFrom;

    @NotNull
    private LocalDateTime effectiveTo;

    // Internal state
    private LocalDateTime lastSentAt;

    private LocalDateTime lastCompletedAt; // last time user confirmed completion

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getIntervalMinutes() { return intervalMinutes; }
    public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
    public Integer getDailyHour() { return dailyHour; }
    public void setDailyHour(Integer dailyHour) { this.dailyHour = dailyHour; }
    public Integer getDailyMinute() { return dailyMinute; }
    public void setDailyMinute(Integer dailyMinute) { this.dailyMinute = dailyMinute; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDateTime effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDateTime getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDateTime effectiveTo) { this.effectiveTo = effectiveTo; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime lastSentAt) { this.lastSentAt = lastSentAt; }
    public LocalDateTime getLastCompletedAt() { return lastCompletedAt; }
    public void setLastCompletedAt(LocalDateTime lastCompletedAt) { this.lastCompletedAt = lastCompletedAt; }
}

