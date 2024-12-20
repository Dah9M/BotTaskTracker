package telegrambot.task;

import telegrambot.model.Task;
import telegrambot.model.TaskCategory;
import telegrambot.model.TaskPriority;
import telegrambot.model.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository database;

    public TaskService(TaskRepository database) {
        this.database = database;
    }

    public String addTask(Long chatId, String description, Timestamp deadline, String priority, Timestamp creationDate, int deadlineNotificationCount) {
        if (!TaskPriority.isValidPriority(priority)) {
            logger.warn("Пользователь {} ввёл некорректный приоритет: {}", chatId, priority);
            return "Invalid priority. Use Low, Medium, or High.";
        }

        Task task = new Task(chatId, description, deadline, TaskPriority.valueOf(priority.toUpperCase()), creationDate, deadlineNotificationCount, TaskCategory.NOT_SPECIFIED);
        try {
            database.addTask(task);
            logger.info("Пользователь {} успешно добавил задачу: {}", chatId, description);
            return "Task added successfully!";
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении задачи для chatId: {}", chatId, e);
            return "Error while adding task.";
        }
    }

    public boolean isTaskOwner(Long chatId, int dbID) {
        List<TaskData> tasks = database.getTasks(chatId, "allTasks");
        boolean isOwner = tasks.stream().anyMatch(task -> task.getDbID() == dbID);
        logger.debug("Проверка владения задачей ID {} пользователем {}: {}", dbID, chatId, isOwner);
        return isOwner;
    }

    public List<TaskData> getTasksByKey(Long chatId, String key) {
        List<TaskData> tasks = database.getTasks(chatId, key);
        if (tasks.isEmpty()) {
            logger.error("Ошибка при получении задач по ключу {} для chatId: {}. Причина: No tasks found.", key, chatId);
            return Collections.emptyList();
        }
        logger.info("Получено {} задач для chatId {} по ключу {}", tasks.size(), chatId, key);
        return tasks;
    }

    public String updateTaskField(Long chatId, int dbID, String field, String newValue) {
        if (!isTaskOwner(chatId, dbID)) {
            logger.warn("Пользователь {} попытался обновить задачу ID {}, которой он не владеет.", chatId, dbID);
            return "You are not the owner of this task.";
        }

        try {
            // Проверяем наличие задачи по dbID
            List<TaskData> tasks = database.getTasks(chatId, "allTasks");
            TaskData taskToUpdate = tasks.stream()
                    .filter(task -> task.getDbID() == dbID)
                    .findFirst()
                    .orElse(null);

            if (taskToUpdate == null) {
                logger.warn("Задача ID {} не найдена для chatId {}", dbID, chatId);
                return "Task ID not found.";
            }

            // Проверка и обновление поля задачи
            switch (field.toLowerCase()) {
                case "description":
                    logger.debug("Пользователь {} обновляет описание задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, newValue);

                case "priority":
                    if (!TaskPriority.isValidPriority(newValue)) {
                        logger.warn("Пользователь {} ввёл некорректный приоритет при обновлении задачи ID {}: {}", chatId, dbID, newValue);
                        return "Invalid priority. Please enter Low, Medium, or High.";
                    }
                    logger.debug("Пользователь {} обновляет приоритет задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, TaskPriority.valueOf(newValue.toUpperCase()).name());

                case "category":
                    if (!TaskCategory.isValidCategory(newValue)) {
                        logger.warn("Пользователь {} ввёл некорректную категорию при обновлении задачи ID {}: {}", chatId, dbID, newValue);
                        return "Invalid category value.";
                    }
                    logger.debug("Пользователь {} обновляет категорию задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, TaskCategory.valueOf(newValue.toUpperCase()).name());

                case "deadline":
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    logger.debug("Пользователь {} обновляет дедлайн задачи ID {} на: {}", chatId, dbID, newValue);
                    return database.updateTaskField(dbID, field, deadline);

                default:
                    logger.warn("Пользователь {} попытался обновить некорректное поле '{}' задачи ID {}", chatId, field, dbID);
                    return "Invalid field. Only 'description', 'priority', 'category', or 'deadline' can be updated.";
            }
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка при попытке ввести дату неверного формата пользователем {}: {}", chatId, newValue, e);
            return "Invalid input format. For deadline, use YYYY-MM-DD HH:MM:SS.";
        } catch (Exception e) {
            logger.error("Ошибка при обновлении задачи ID {} для chatId {}.", dbID, chatId, e);
            return "An error occurred while updating the task.";
        }
    }

    public String deleteTask(Long chatId, int dbId) {
        if (!isTaskOwner(chatId, dbId)) {
            logger.warn("Пользователь {} попытался удалить задачу ID {}, которой он не владеет.", chatId, dbId);
            return "You are not the owner of this task.";
        }

        try {
            boolean success = database.deleteTask(dbId);
            if (success) {
                logger.info("Пользователь {} успешно удалил задачу ID {}.", chatId, dbId);
                return "Task deleted successfully!";
            } else {
                logger.warn("Задача ID {} не найдена для удаления пользователем {}.", dbId, chatId);
                return "Task not found.";
            }
        } catch (SQLException e) {
            logger.error("Ошибка при удалении задачи ID {} для chatId {}.", dbId, chatId, e);
            return "Error while deleting task.";
        }
    }

    // для получения инфы о тасках
    public List<TaskData> getTasks(Long chatId, String status) {
        if (chatId == null) {
            logger.error("Ошибка при получении задач по приоритету для chatId: {}. Причина: chatId is null.", chatId);
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        List<TaskData> tasks = database.getTasks(chatId, status);
        logger.info("Получено {} задач для chatId {} по статусу {}", tasks.size(), chatId, status);
        return tasks;
    }

    public boolean updateTaskNotificationCount(int taskId, int newCount) {
        boolean updated = database.updateTaskNotificationCount(taskId, newCount);
        if (updated) {
            logger.info("Обновлён deadlineNotificationCount для задачи ID {}: новый счётчик {}", taskId, newCount);
        } else {
            logger.warn("Не удалось обновить deadlineNotificationCount для задачи ID {}.", taskId);
        }
        return updated;
    }
}
