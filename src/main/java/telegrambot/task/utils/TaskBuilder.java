package telegrambot.task.utils;

import lombok.NonNull;
import telegrambot.task.TaskData;
import telegrambot.task.TaskService;
import telegrambot.model.TaskPriority;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TaskBuilder implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    private static final ZoneId ZONE_UTC_PLUS_5 = ZoneId.of("UTC+05:00");

    public TaskBuilder(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(@NonNull Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        log.info("Пользователь {} начал процесс создания задачи.", chatId);
        return "Please provide a description for the task.";
    }

    @Override
    public String processInput(@NonNull Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            log.warn("Пользователь {} попытался ввести данные без инициализации процесса создания задачи.", chatId);
            return "Task creation process has not started. Please initiate by clicking 'Add Task'.";
        }

        switch (taskData.getStep()) {
            case 0:
                taskData.setDescription(input);
                taskData.nextStep();
                log.debug("Пользователь {} ввёл описание задачи: {}", chatId, input);
                return "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS).";

            case 1:
                try {
                    LocalDateTime userInputTime = LocalDateTime.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    ZonedDateTime userDeadlineInZone = userInputTime.atZone(ZONE_UTC_PLUS_5);

                    ZonedDateTime nowInZone = ZonedDateTime.now(ZONE_UTC_PLUS_5);
                    if (userDeadlineInZone.isBefore(nowInZone)) {
                        log.warn("Пользователь {} ввёл дедлайн в прошлом: {}", chatId, input);
                        return "The deadline is in the past. Please provide a future date and time.";
                    }

                    taskData.setDeadline(Timestamp.valueOf(userInputTime));
                    taskData.nextStep();
                    log.debug("Пользователь {} установил дедлайн задачи: {}", chatId, input);
                    return "Please provide a priority for the task (e.g., High, Medium, Low).";

                } catch (Exception e) {
                    log.error("Неверное форматирование времени пользователем {}: {}", chatId, input, e);
                    return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                }

            case 2:
                if (!TaskPriority.isValidPriority(input)) {
                    log.warn("Пользователь {} ввёл некорректный приоритет: {}", chatId, input);
                    return "Invalid priority. Please enter one of the following: Low, Medium, High.";
                }

                taskData.setPriority(TaskPriority.valueOf(input.toUpperCase()).name());
                taskData.setCreationDate(new Timestamp(System.currentTimeMillis()));
                taskData.setDeadlineNotificationCount(0); // Инициализируем счетчик
                log.info("Пользователь {} установил приоритет задачи: {}", chatId, input);

                String result = taskService.addTask(
                        taskData.getChatId(),
                        taskData.getDescription(),
                        taskData.getDeadline(),
                        taskData.getPriority().name(),
                        taskData.getCreationDate(),
                        taskData.getDeadlineNotificationCount()
                );
                taskDataMap.remove(chatId);
                log.info("Пользователь {} завершил процесс создания задачи: {}", chatId, result);
                return result;

            default:
                log.warn("Пользователь {} ввёл неожиданный шаг процесса создания задачи: {}", chatId, taskData.getStep());
                return "Unexpected input.";
        }
    }

    @Override
    public void clearOperationData(@NonNull Long chatId) {
        taskDataMap.remove(chatId);
        log.info("Данные процесса создания задачи для пользователя {} очищены.", chatId);
    }

    @Override
    public boolean isInProgress(@NonNull Long chatId) {
        boolean inProgress = taskDataMap.containsKey(chatId);
        log.debug("Проверка процесса создания задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    @Override
    public TaskData getTaskData(Long chatId) {
        return taskDataMap.get(chatId);
    }
}
