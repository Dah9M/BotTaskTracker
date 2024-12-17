package TelegramBot.task.utils;

import TelegramBot.model.TaskPriority;
import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;
import TelegramBot.utils.LoggerFactoryUtil;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.Instant;

public class TaskBuilder implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;



    private static final ZoneId ZONE_UTC_PLUS_5 = ZoneId.of("UTC+05:00");

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
                return "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS).";

            case 1:
                try {
                    LocalDateTime userInputTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ZonedDateTime userDeadlineInZone = userInputTime.atZone(ZONE_UTC_PLUS_5);

                    ZonedDateTime nowInZone = ZonedDateTime.now(ZONE_UTC_PLUS_5);
                    if (userDeadlineInZone.isBefore(nowInZone)) {
                        return "The deadline is in the past. Please provide a future date and time.";
                    }

                    // Конвертация в Timestamp для сохранения
                    taskData.setDeadline(Timestamp.valueOf(userInputTime));
                    taskData.nextStep();
                    return "Please provide a priority for the task (e.g., High, Medium, Low).";

                } catch (Exception e) {
                    LoggerFactoryUtil.logError("Неверное форматирование времени пользователем: {}", e, chatId);
                    return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                }

            case 2:
                if (!TaskPriority.isValidPriority(input)) {
                    return "Invalid priority. Please enter one of the following: Low, Medium, High.";
                }

                taskData.setPriority(TaskPriority.valueOf(input.toUpperCase()).name());
                taskData.setCreationDate(new Timestamp(System.currentTimeMillis()));
                taskData.setDeadlineNotificationCount(0); // Инициализируем счетчик

                String result = taskService.addTask(
                        taskData.getChatId(),
                        taskData.getDescription(),
                        taskData.getDeadline(),
                        taskData.getPriority().name(),
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
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    @Override
    public boolean isInProgress(Long chatId) {
        return taskDataMap.containsKey(chatId);
    }

    @Override
    public TaskData getTaskData(Long chatId) {
        return taskDataMap.get(chatId);
    }
}
