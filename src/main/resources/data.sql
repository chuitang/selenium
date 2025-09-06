INSERT INTO reminder_task (id, name, remind_time, repeat_interval_minutes, content, phone_number, status, effective_from, effective_to, last_completed_date, last_notified_at, created_at, updated_at)
VALUES (1, '吃药提醒', '19:00:00', 30, '请按时吃药', '+8613800000000', 'ACTIVE', CURRENT_DATE, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

