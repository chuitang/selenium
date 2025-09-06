package com.example.smsreminder.repository;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.model.ReminderTask.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReminderTaskRepository extends JpaRepository<ReminderTask, Long> {
    List<ReminderTask> findByStatus(Status status);
    List<ReminderTask> findByStatusAndEffectiveFromLessThanEqual(Status status, LocalDate date);
    Page<ReminderTask> findByNameContainingIgnoreCase(String name, Pageable pageable);
}

