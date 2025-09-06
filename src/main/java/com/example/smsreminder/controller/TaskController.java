package com.example.smsreminder.controller;

import com.example.smsreminder.model.ReminderTask;
import com.example.smsreminder.repository.ReminderTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final ReminderTaskRepository repository;

    public TaskController(ReminderTaskRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {
        Page<ReminderTask> pageData;
        if (query == null || query.trim().isEmpty()) {
            pageData = repository.findAll(PageRequest.of(page, size));
        } else {
            pageData = repository.findByNameContainingIgnoreCase(query.trim(), PageRequest.of(page, size));
        }
        model.addAttribute("page", pageData);
        model.addAttribute("q", query == null ? "" : query);
        return "tasks/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("task", new ReminderTask());
        return "tasks/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("task") ReminderTask task, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "tasks/form";
        }
        repository.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Optional<ReminderTask> task = repository.findById(id);
        if (!task.isPresent()) return "redirect:/tasks";
        model.addAttribute("task", task.get());
        return "tasks/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("task") ReminderTask task,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "tasks/form";
        }
        task.setId(id);
        repository.save(task);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/tasks";
    }
}

