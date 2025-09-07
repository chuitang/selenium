package com.example.smsreminder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Support HTML5 datetime-local binding
        registry.addConverter(String.class, LocalDateTime.class, (String source) -> {
            if (source == null || source.trim().isEmpty()) return null;
            // e.g. 2025-01-01T19:00
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            return LocalDateTime.parse(source, f);
        });
    }
}

