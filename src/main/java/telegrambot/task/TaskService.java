package telegrambot.task;

import lombok.NonNull;
import telegrambot.model.Task;
import telegrambot.model.TaskCategory;
import telegrambot.model.TaskPriority;
import telegrambot.model.TaskRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@Slf4j
public class TaskService {
    private final TaskRepository database;

    public TaskService(TaskRepository database) {
        this.database = database;
    }

    public String addTask(@NonNull Long chatId, @NonNull String description, @NonNull Timestamp deadline,
                          String priority, Timestamp creationDate, int deadlineNotificationCount) {
        if (!TaskPriority.isValidPriority(priority)) {
            log.warn("Пользователь {} ввёл некорректный приоритет: {}", chatId, priority);
            return "Invalid priority. Use Low, Medium, or High.";
        }

        Task task = new Task(chatId, description, deadline, TaskPriority.valueOf(priority.toUpperCase()), creationDate, deadlineNotificationCount, TaskCategory.NOT_SPECIFIED);
        try {
            database.addTask(task);
            log.info("Пользователь {} успешно добавил задачу: {}", chatId, description);
            return "Task added successfully!";
        } catch (SQLException e) {
            log.error("Ошибка при добавлении задачи для chatId: {}", chatId, e);
            return "Error while adding task.";
        }
    }

    public boolean isTaskOwner(@NonNull Long chatId, int dbID) {
        List<TaskData> tasks = database.getTasks(chatId, "allTasks");
        boolean isOwner = tasks.stream().anyMatch(task -> task.getDbID() == dbID);
        log.debug("Проверка владения задачей ID {} пользователем {}: {}", dbID, chatId, isOwner);
        return isOwner;
    }

    public List<TaskData> getTasksByKey(@NonNull Long chatId, String key) {
        List<TaskData> tasks = database.getTasks(chatId, key);
        if (tasks.isEmpty()) {
            log.error("Ошибка при получении задач по ключу {} для chatId: {}. Причина: No tasks found.", key, chatId);
            return Collections.emptyList();
        }
        log.info("Получено {} задач для chatId {} по ключу {}", tasks.size(), chatId, key);
        return tasks;
    }

    public String updateTaskField(@NonNull Long chatId, int dbID, String field, String newValue) {
        if (!isTaskOwner(chatId, dbID)) {
            log.warn("Пользователь {} попытался обновить задачу ID {}, которой он не владеет.", chatId, dbID);
            return "You are not the owner of this task.";
        }

        try {
            List<TaskData> tasks = database.getTasks(chatId, "allTasks");
            TaskData taskToUpdate = tasks.stream()
                    .filter(task -> task.getDbID() == dbID)
                    .findFirst()
                    .orElse(null);

            if (taskToUpdate == null) {
                log.warn("Задача ID {} не найдена для chatId {}", dbID, chatId);
                return "Task ID not found.";
            }

            switch (field.toLowerCase()) {
                case "description":
                    log.debug("Пользователь {} обновляет описание задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, newValue);

                case "priority":
                    if (!TaskPriority.isValidPriority(newValue)) {
                        log.warn("Пользователь {} ввёл некорректный приоритет при обновлении задачи ID {}: {}", chatId, dbID, newValue);
                        return "Invalid priority. Please enter Low, Medium, or High.";
                    }
                    log.debug("Пользователь {} обновляет приоритет задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, TaskPriority.valueOf(newValue.toUpperCase()).name());

                case "category":
                    if (!TaskCategory.isValidCategory(newValue)) {
                        log.warn("Пользователь {} ввёл некорректную категорию при обновлении задачи ID {}: {}", chatId, dbID, newValue);
                        return "Invalid category value.";
                    }
                    log.debug("Пользователь {} обновляет категорию задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, TaskCategory.valueOf(newValue.toUpperCase()).name());

                case "deadline":
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    log.debug("Пользователь {} обновляет дедлайн задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, deadline);

                default:
                    log.warn("Пользователь {} попытался обновить некорректное поле '{}' задачи ID {}", chatId, field, dbID);
                    return "Invalid field. Only 'description', 'priority', 'category', or 'deadline' can be updated.";
            }
        } catch (IllegalArgumentException e) {
            log.error("Ошибка при попытке ввести дату неверного формата пользователем {}: {}", chatId, newValue, e);
            return "Invalid input format. For deadline, use YYYY-MM-DD HH:MM:SS.";
        } catch (Exception e) {
            log.error("Ошибка при обновлении задачи ID {} для chatId {}.", dbID, chatId, e);
            return "An error occurred while updating the task.";
        }
    }

    public String deleteTask(@NonNull Long chatId, int dbId) {
        if (!isTaskOwner(chatId, dbId)) {
            log.warn("Пользователь {} попытался удалить задачу ID {}, которой он не владеет.", chatId, dbId);
            return "You are not the owner of this task.";
        }

        try {
            boolean success = database.deleteTask(dbId);
            if (success) {
                log.info("Пользователь {} успешно удалил задачу ID {}.", chatId, dbId);
                return "Task deleted successfully!";
            } else {
                log.warn("Задача ID {} не найдена для удаления пользователем {}.", dbId, chatId);
                return "Task not found.";
            }
        } catch (SQLException e) {
            log.error("Ошибка при удалении задачи ID {} для chatId {}.", dbId, chatId, e);
            return "Error while deleting task.";
        }
    }

    public List<TaskData> getTasks(@NonNull Long chatId, String status) {
        if (chatId == null) {
            log.error("Ошибка при получении задач по приоритету для chatId: {}. Причина: chatId is null.", chatId);
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        List<TaskData> tasks = database.getTasks(chatId, status);
        log.info("Получено {} задач для chatId {} по статусу {}", tasks.size(), chatId, status);
        return tasks;
    }

    public boolean updateTaskNotificationCount(int taskId, int newCount) {
        boolean updated = database.updateTaskNotificationCount(taskId, newCount);
        if (updated) {
            log.info("Обновлён deadlineNotificationCount для задачи ID {}: новый счётчик {}", taskId, newCount);
        } else {
            log.warn("Не удалось обновить deadlineNotificationCount для задачи ID {}.", taskId);
        }
        return updated;
    }
}
