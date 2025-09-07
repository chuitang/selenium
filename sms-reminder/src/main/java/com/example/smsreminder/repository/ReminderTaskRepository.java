package com.example.smsreminder.repository;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderTaskRepository extends JpaRepository<ReminderTask, Long> {

    List<ReminderTask> findByStatus(TaskStatus status);

    @Query("select t from ReminderTask t where t.status = 'ACTIVE' and t.effectiveFrom <= :now and t.effectiveTo >= :now")
    List<ReminderTask> findActiveEffective(LocalDateTime now);
}

