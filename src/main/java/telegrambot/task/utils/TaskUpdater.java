package telegrambot.task.utils;

import lombok.NonNull;
import telegrambot.model.TaskCategory;
import telegrambot.model.TaskPriority;
import telegrambot.task.TaskData;
import telegrambot.task.TaskService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TaskUpdater implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskUpdater(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(@NonNull Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        log.info("Пользователь {} начал процесс обновления задачи.", chatId);
        return "Please provide the ID of the task you want to update.";
    }

    @Override
    public String processInput(@NonNull Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            log.warn("Пользователь {} попытался обновить задачу без инициализации процесса.", chatId);
            return "Task update not initiated.";
        }

        switch (taskData.getStep()) {
            case 0:
                try {
                    int dbID = Integer.parseInt(input);
                    taskData.setDbID(dbID);
                    if (!taskService.isTaskOwner(chatId, dbID)) {
                        taskDataMap.remove(chatId);
                        log.warn("Пользователь {} попытался обновить задачу с ID {}, которой он не владеет.", chatId, dbID);
                        return "You are not the owner of this task.";
                    }
                    taskData.nextStep();
                    log.debug("Пользователь {} выбрал задачу с ID {} для обновления.", chatId, dbID);
                    return "Please select the field to update: Description, Deadline, Category or Priority.";
                } catch (NumberFormatException e) {
                    log.error("Пользователь {} ввёл некорректный ID задачи: {}", chatId, input, e);
                    return "Invalid task ID format. Please enter a numeric ID.";
                }

            case 1:
                taskData.setSelectedField(input.toLowerCase());
                taskData.nextStep();
                log.debug("Пользователь {} выбрал поле '{}' для обновления.", chatId, input);
                switch (input.toLowerCase()) {
                    case "description":
                        return "Please enter the new description for the task.";
                    case "deadline":
                        return "Please enter the new deadline for the task (YYYY-MM-DD HH:MM:SS).";
                    case "category":
                        return "Please enter the new category for the task (Work, Life, Education).";
                    case "priority":
                        return "Please enter the new priority for the task (Low, Medium, High).";
                    default:
                        log.warn("Пользователь {} выбрал некорректное поле '{}'.", chatId, input);
                        return "Invalid field. Please choose from Description, Deadline, Category, or Priority.";
                }

            case 2:
                String selectedField = taskData.getSelectedField();
                String newValue;

                switch (selectedField) {
                    case "description":
                        newValue = input;
                        break;
                    case "deadline":
                        try {
                            newValue = input;
                        } catch (Exception e) {
                            log.error("Пользователь {} ввёл неверный формат дедлайна: {}", chatId, input, e);
                            return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                        }
                        break;
                    case "category":
                        if (!TaskCategory.isValidCategory(input)) {
                            log.warn("Пользователь {} ввёл некорректную категорию: {}", chatId, input);
                            return "Invalid category. Please enter Work, Life, or Education.";
                        }
                        newValue = TaskCategory.valueOf(input.toUpperCase()).name();
                        break;
                    case "priority":
                        if (!TaskPriority.isValidPriority(input)) {
                            log.warn("Пользователь {} ввёл некорректный приоритет: {}", chatId, input);
                            return "Invalid priority. Please enter Low, Medium, or High.";
                        }
                        newValue = TaskPriority.valueOf(input.toUpperCase()).name();
                        break;
                    default:
                        log.warn("Пользователь {} выбрал некорректное поле '{}'.", chatId, selectedField);
                        return "Invalid field selected.";
                }

                String result = taskService.updateTaskField(chatId, taskData.getDbID(), selectedField, newValue);
                taskDataMap.remove(chatId);
                log.info("Пользователь {} завершил обновление задачи с ID {}: {}", chatId, taskData.getDbID(), result);
                return result;

            default:
                log.warn("Пользователь {} ввёл неожиданный шаг процесса обновления задачи: {}", chatId, taskData.getStep());
                return "Unexpected input.";
        }
    }

    @Override
    public void clearOperationData(@NonNull Long chatId) {
        taskDataMap.remove(chatId);
        log.info("Данные процесса обновления задачи для пользователя {} очищены.", chatId);
    }

    @Override
    public boolean isInProgress(@NonNull Long chatId) {
        boolean inProgress = taskDataMap.containsKey(chatId);
        log.debug("Проверка процесса обновления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    @Override
    public TaskData getTaskData(@NonNull Long chatId) {
        return taskDataMap.get(chatId);
    }
}
