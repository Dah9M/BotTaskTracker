package TelegramBot.task;

import TelegramBot.model.Task;
import TelegramBot.model.TaskPriority;
import TelegramBot.model.TaskRepository;

import java.sql.SQLException;
import java.sql.Timestamp;
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
            return "Error while adding task.";
        }
    }

    public boolean isTaskOwner(Long chatId, int dbID) {
        List<TaskData> tasks = database.getTasks(chatId, "allTasks");
        return tasks.stream().anyMatch(task -> task.getDbID() == dbID);
    }

    public String getTasksByStatus(Long chatId, String status) {
        List<TaskData> tasks = database.getTasks(chatId, status);
        if (tasks.isEmpty()) {
            return "No tasks found.";
        }
        StringBuilder taskList = new StringBuilder("Tasks:\n");
        for (TaskData task : tasks) {
            taskList.append(task.toString()).append("\n"); // Переопределите метод toString() в Task для форматирования
        }
        return taskList.toString();
    }

    public String getTasksByCategory(Long chatId, String category) {
        List<TaskData> tasks;
        if ("all".equalsIgnoreCase(category)) {
            tasks = database.getTasks(chatId, "allTasks");
        } else {
            tasks = database.getTasksByCategory(chatId, category);
        }

        if (tasks.isEmpty()) {
            return "No tasks found for category: " + category;
        }

        StringBuilder taskList = new StringBuilder("Tasks in category '" + category + "':\n");
        for (TaskData task : tasks) {
            taskList.append(task.toString()).append("\n");
        }
        return taskList.toString();
    }

    public String getTasksByPriority(Long chatId, String priority) {
        List<TaskData> tasks;
        if ("All".equalsIgnoreCase(priority)) {
            tasks = database.getTasks(chatId, "allTasks");
        } else {
            tasks = database.getTasksByPriority(chatId, priority.toUpperCase());
        }

        if (tasks.isEmpty()) {
            return "No tasks found with priority: " + priority;
        }

        StringBuilder taskList = new StringBuilder("Tasks with priority '" + priority + "':\n");
        for (TaskData task : tasks) {
            taskList.append(task.toString()).append("\n");
        }
        return taskList.toString();
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

            long trueId = taskToUpdate.getDbID();

            // Обновляем поле задачи в базе данных
            switch (field.toLowerCase()) {
                case "description":
                    return database.updateTaskField(dbID, field, newValue);
                case "priority":
                    return database.updateTaskField(dbID, field, newValue);
                case "deadline":
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    return database.updateTaskField(dbID, field, deadline);
                default:
                    return "Invalid field.";
            }
        } catch (IllegalArgumentException e) {
            return "Invalid deadline format. Use YYYY-MM-DD HH:MM:SS.";
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
            e.printStackTrace();
            return "Error while deleting task.";
        }
    }

    // для получения инфы о тасках
    public List<TaskData> getTasks(Long chatId, String status) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        return database.getTasks(chatId, status);
    }
    public void updateTaskNotificationCount(int taskId, int newCount) {
        database.updateTaskNotificationCount(taskId, newCount);
    }
}
