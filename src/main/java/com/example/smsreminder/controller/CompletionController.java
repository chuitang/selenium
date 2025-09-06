package com.example.smsreminder.controller;

import com.example.smsreminder.service.ReminderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
public class CompletionController {

    private final ReminderService reminderService;

    public CompletionController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/{id}/complete")
    public String confirmPage(@PathVariable("id") Long taskId,
                              @RequestParam("d") String dateStr,
                              @RequestParam("t") String token,
                              Model model) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("dateStr", dateStr);
        model.addAttribute("token", token);
        return "tasks/complete";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable("id") Long taskId,
                           @RequestParam("d") String dateStr,
                           @RequestParam("t") String token,
                           Model model) {
        LocalDate date = LocalDate.parse(dateStr);
        boolean ok = reminderService.completeTask(taskId, date, token);
        if (ok) {
            return "redirect:/tasks?completed=1";
        }
        model.addAttribute("taskId", taskId);
        model.addAttribute("dateStr", dateStr);
        model.addAttribute("token", token);
        model.addAttribute("error", true);
        return "tasks/complete";
    }
}

