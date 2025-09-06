package com.example.smsreminder.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.*;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "reminder_task")
public class ReminderTask {

    public enum Status { ACTIVE, PAUSED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // Time of day to start reminding (e.g. 19:00)
    @NotNull
    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime remindTime;

    // Minutes between reminders within the same day if not completed
    @NotNull
    @Min(1)
    @Max(1440)
    @Column(nullable = false)
    private Integer repeatIntervalMinutes = 30;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String content;

    @NotBlank
    @Pattern(regexp = "^\\+?\\d{6,20}$", message = "Invalid phone number")
    @Column(nullable = false)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @NotNull
    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveTo; // nullable => no expiry

    private LocalDate lastCompletedDate; // date for which user confirmed completion

    private LocalDateTime lastNotifiedAt; // when last SMS was sent

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActiveNow(LocalDateTime now) {
        if (status != Status.ACTIVE) return false;
        LocalDate today = now.toLocalDate();
        if (effectiveFrom != null && today.isBefore(effectiveFrom)) return false;
        if (effectiveTo != null && today.isAfter(effectiveTo)) return false;
        return true;
    }

    public boolean isCompletedForDate(LocalDate date) {
        return lastCompletedDate != null && lastCompletedDate.equals(date);
    }

    public boolean shouldNotify(LocalDateTime now) {
        if (!isActiveNow(now)) return false;
        LocalDate today = now.toLocalDate();
        if (isCompletedForDate(today)) return false;
        LocalDateTime firstReminderTime = LocalDateTime.of(today, remindTime);
        if (now.isBefore(firstReminderTime)) return false;
        if (lastNotifiedAt == null) return true;
        // Only allow reminders for today (do not spam next day if missed)
        if (!lastNotifiedAt.toLocalDate().equals(today)) return true;
        Duration since = Duration.between(lastNotifiedAt, now);
        return since.toMinutes() >= repeatIntervalMinutes;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalTime getRemindTime() { return remindTime; }
    public void setRemindTime(LocalTime remindTime) { this.remindTime = remindTime; }

    public Integer getRepeatIntervalMinutes() { return repeatIntervalMinutes; }
    public void setRepeatIntervalMinutes(Integer repeatIntervalMinutes) { this.repeatIntervalMinutes = repeatIntervalMinutes; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public LocalDate getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(LocalDate lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }

    public LocalDateTime getLastNotifiedAt() { return lastNotifiedAt; }
    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) { this.lastNotifiedAt = lastNotifiedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

