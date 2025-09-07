package com.example.smsreminder.controller;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.model.TaskStatus;
import com.example.smsreminder.repository.ReminderTaskRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class TaskController {

    private final ReminderTaskRepository repository;

    public TaskController(ReminderTaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping({"/", "/tasks"})
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        List<ReminderTask> tasks;
        if (query != null && !query.trim().isEmpty()) {
            tasks = repository.findAll().stream()
                    .filter(t -> t.getName() != null && t.getName().contains(query))
                    .collect(Collectors.toList());
        } else {
            tasks = repository.findAll();
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("q", query == null ? "" : query);
        return "tasks/list";
    }

    @GetMapping("/tasks/new")
    public String createForm(Model model) {
        ReminderTask task = new ReminderTask();
        task.setStatus(TaskStatus.ACTIVE);
        task.setEffectiveFrom(LocalDateTime.now().minusMinutes(1));
        task.setEffectiveTo(LocalDateTime.now().plusYears(1));
        task.setIntervalMinutes(30);
        task.setDailyHour(19);
        task.setDailyMinute(0);
        model.addAttribute("task", task);
        model.addAttribute("statuses", TaskStatus.values());
        return "tasks/form";
    }

    @PostMapping("/tasks")
    public String create(@Valid @ModelAttribute("task") ReminderTask task, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            return "tasks/form";
        }
        repository.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<ReminderTask> opt = repository.findById(id);
        if (!opt.isPresent()) {
            return "redirect:/tasks";
        }
        model.addAttribute("task", opt.get());
        model.addAttribute("statuses", TaskStatus.values());
        return "tasks/form";
    }

    @PostMapping("/tasks/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("task") ReminderTask form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", TaskStatus.values());
            return "tasks/form";
        }
        form.setId(id);
        repository.save(form);
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/tasks";
    }
}

