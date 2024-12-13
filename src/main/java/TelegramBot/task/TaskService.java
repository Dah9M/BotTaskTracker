package TelegramBot.task;

import TelegramBot.model.Task;
import TelegramBot.model.TaskCategory;
import TelegramBot.model.TaskPriority;
import TelegramBot.model.TaskRepository;
import TelegramBot.utils.LoggerFactoryUtil;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;


public class TaskService {
    private final TaskRepository database;
    public TaskService(TaskRepository database) {
        this.database = database;
    }

    public String addTask(Long chatId, String description, Timestamp deadline, String priority, Timestamp creationDate, int deadlineNotificationCount) {
        if (!TaskPriority.isValidPriority(priority)) {
            return "Invalid priority. Use Low, Medium, or High.";
        }

        Task task = new Task(chatId, description, deadline, TaskPriority.valueOf(priority.toUpperCase()), creationDate, deadlineNotificationCount);
        try {
            database.addTask(task);
            return "Task added successfully!";
        } catch (SQLException e) {
            LoggerFactoryUtil.logError("Ошибка при добавлении задачи для chatId: {}", e, chatId);
            return "Error while adding task.";
        }
    }

    public boolean isTaskOwner(Long chatId, int dbID) {
        List<TaskData> tasks = database.getTasks(chatId, "allTasks");
        return tasks.stream().anyMatch(task -> task.getDbID() == dbID);
    }

    public List<TaskData> getTasksByStatus(Long chatId, String status) {
        List<TaskData> tasks = database.getTasks(chatId, status);
        if (tasks.isEmpty()) {
            LoggerFactoryUtil.logError("Ошибка при получении задач по статусу для chatId: {}", new Exception("No tasks found"), chatId);
            return Collections.emptyList();
        }
        return tasks;
    }

    public List<TaskData> getTasksByCategory(Long chatId, String category) {
        List<TaskData> tasks;
        if ("all".equalsIgnoreCase(category)) {
            tasks = database.getTasks(chatId, "allTasks");
        } else {
            tasks = database.getTasksByCategory(chatId, category);
        }

        if (tasks.isEmpty()) {
            LoggerFactoryUtil.logError("Ошибка при получении задач по категории для chatId: {}", new Exception("No tasks found"), chatId);
            throw new IllegalArgumentException("No tasks found for category: " + category);
        }

        return tasks;
    }

    public List<TaskData> getTasksByPriority(Long chatId, String priority) {
        List<TaskData> tasks;
        if ("All".equalsIgnoreCase(priority)) {
            tasks = database.getTasks(chatId, "allTasks");
        } else {
            tasks = database.getTasksByPriority(chatId, priority.toUpperCase());
        }

        if (tasks.isEmpty()) {
            LoggerFactoryUtil.logError("Ошибка при получении задач по приоритету для chatId: {}", new Exception("No tasks found"), chatId);
            throw new IllegalArgumentException("No tasks found for priority: " + priority);
        }

        return tasks;
    }

    public String updateTaskField(Long chatId, int dbID, String field, String newValue) {
        if (!isTaskOwner(chatId, dbID)) {
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
                return "Task ID not found.";
            }

            // Проверка и обновление поля задачи
            switch (field.toLowerCase()) {
                case "description":
                    return database.updateTaskField(dbID, field, newValue);

                case "priority":
                    if (!TaskPriority.isValidPriority(newValue)) {
                        return "Invalid priority. Please enter Low, Medium, or High.";
                    }
                    return database.updateTaskField(dbID, field, TaskPriority.valueOf(newValue.toUpperCase()).name());

                case "category":
                    if (!TaskCategory.isValidCategory(newValue)) {
                        return "Invalid category value.";
                    }
                    return database.updateTaskField(dbID, field, TaskCategory.valueOf(newValue.toUpperCase()).name());


                case "deadline":
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    return database.updateTaskField(dbID, field, deadline);

                default:
                    return "Invalid field. Only 'description', 'priority', 'category', or 'deadline' can be updated.";
            }
        } catch (IllegalArgumentException e) {
            LoggerFactoryUtil.logError("Ошибка при попытке ввести дату неверного формата: {}", e, chatId);
            return "Invalid input format. For deadline, use YYYY-MM-DD HH:MM:SS.";
        } catch (Exception e) {
            LoggerFactoryUtil.logError("Ошибка при обновлении задачи: {}", e, chatId);
            return "An error occurred while updating the task.";
        }
    }


    public String deleteTask(Long chatId, int dbId) {
        if (!isTaskOwner(chatId, dbId)) {
            return "You are not the owner of this task.";
        }

        try {
            boolean success = database.deleteTask(dbId);
            return success ? "Task deleted successfully!" : "Task not found.";
        } catch (SQLException e) {
            LoggerFactoryUtil.logError("Ошибка при удалении задачи: {}", e, chatId);
            return "Error while deleting task.";
        }
    }

    // для получения инфы о тасках
    public List<TaskData> getTasks(Long chatId, String status) {
        if (chatId == null) {
            LoggerFactoryUtil.logError("Ошибка при получении задач по приоритету для chatId: {}", new Exception("No tasks found"), chatId);

        }
        return database.getTasks(chatId, status);
    }
    public void updateTaskNotificationCount(int taskId, int newCount) {
        database.updateTaskNotificationCount(taskId, newCount);
    }
}
