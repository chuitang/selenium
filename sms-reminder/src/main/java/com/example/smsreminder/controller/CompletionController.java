package com.example.smsreminder.controller;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.repository.ReminderTaskRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class CompletionController {

    private final ReminderTaskRepository repository;

    public CompletionController(ReminderTaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/complete")
    public String complete(@RequestParam("taskId") Long taskId, Model model) {
        Optional<ReminderTask> opt = repository.findById(taskId);
        if (!opt.isPresent()) {
            model.addAttribute("message", "任务不存在");
            return "complete/prompt";
        }
        ReminderTask task = opt.get();
        model.addAttribute("task", task);
        return "complete/prompt";
    }

    @GetMapping("/complete/confirm")
    public String confirm(@RequestParam("taskId") Long taskId, Model model) {
        Optional<ReminderTask> opt = repository.findById(taskId);
        if (!opt.isPresent()) {
            model.addAttribute("message", "任务不存在");
            return "complete/result";
        }
        ReminderTask task = opt.get();
        task.setLastCompletedAt(LocalDateTime.now());
        repository.save(task);
        model.addAttribute("message", "已确认完成，本周期内不再提醒。");
        return "complete/result";
    }
}

