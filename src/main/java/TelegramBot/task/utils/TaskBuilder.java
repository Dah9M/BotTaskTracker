package TelegramBot.task.utils;

import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

public class TaskBuilder implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    // Используем ZoneId для UTC+5
    private static final ZoneId UTC_PLUS_5 = ZoneId.of("UTC+05:00");

    public TaskBuilder(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        return "Please provide a description for the task.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            return "Task creation process has not started. Please initiate by clicking 'Add Task'.";
        }

        switch (taskData.getStep()) {
            case 0:
                taskData.setDescription(input);
                taskData.nextStep();
                return "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS) in UTC+5.";

            case 1:
                try {
                    // Парсим введенное пользователем время как локальное UTC+5
                    LocalDateTime userInputTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ZonedDateTime userDeadlineInZone = userInputTime.atZone(UTC_PLUS_5);

                    ZonedDateTime nowInZone = ZonedDateTime.now(UTC_PLUS_5);
                    if (userDeadlineInZone.isBefore(nowInZone)) {
                        return "The deadline is in the past. Please provide a future date and time.";
                    }

                    // Сохраняем в БД как есть, без перевода в UTC, интерпретируем как локальное время (UTC+5)
                    Timestamp deadline = Timestamp.valueOf(userInputTime);
                    taskData.setDeadline(deadline);
                    taskData.nextStep();
                    return "Please provide a priority for the task (e.g., High, Medium, Low).";

                } catch (Exception e) {
                    return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                }

            case 2:
                taskData.setPriority(input);
                taskData.setCreationDate(new Timestamp(System.currentTimeMillis()));
                taskData.setDeadlineNotificationCount(0);

                String result = taskService.addTask(
                        taskData.getChatId(),
                        taskData.getDescription(),
                        taskData.getDeadline(),
                        taskData.getPriority(),
                        taskData.getCreationDate(),
                        taskData.getDeadlineNotificationCount()
                );
                taskDataMap.remove(chatId);
                return result;

            default:
                return "Unexpected input.";
        }
    }

    @Override
    public boolean isOperationCompleted(Long chatId) {
        TaskData taskData = taskDataMap.get(chatId);
        return taskData != null && taskData.getStep() >= 3;
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    @Override
    public boolean isInProgress(Long chatId) {
        return taskDataMap.containsKey(chatId);
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
