package TelegramBot.task;

import TelegramBot.model.Task;
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
        Task task = new Task(chatId, description, deadline, priority, creationDate, deadlineNotificationCount);
        try {
            database.addTask(task);
            return "Task added successfully!";
        } catch (SQLException e) {
            return "Error while adding task.";
        }
    }

    public String getTasksByStatus(Long chatId, String status) {
        List<TaskData> tasks = database.getTasks(chatId, status);
        if (tasks.isEmpty()) {
            return "No tasks found.";
        }
        StringBuilder taskList = new StringBuilder("Tasks:\n");
        for (TaskData task : tasks) {
            taskList.append(task.toString()).append("\n");
        }
        return taskList.toString();
    }

    public String updateTaskField(Long chatId, int dbID, String field, String newValue) {
        try {
            List<TaskData> tasks = database.getTasks(chatId, "allTasks");
            TaskData taskToUpdate = tasks.stream()
                    .filter(task -> task.getDbID() == dbID)
                    .findFirst()
                    .orElse(null);

            if (taskToUpdate == null) {
                return "Task ID not found.";
            }

            long trueId = taskToUpdate.getDbID();

            switch (field.toLowerCase()) {
                case "description":
                case "priority":
                    return database.updateTaskField(trueId, field, newValue);
                case "deadline":
                    Timestamp deadline = TelegramBot.utils.TimeConverter.convertFromUTCPlus5ToUTC(newValue);
                    return database.updateTaskField(trueId, field, deadline);
                default:
                    return "Invalid field.";
            }
        } catch (IllegalArgumentException e) {
            return "Invalid deadline format. Use YYYY-MM-DD HH:MM:SS.";
        }
    }

    public String deleteTask(Long taskId) {
        try {
            boolean success = database.deleteTask(taskId);
            return success ? "Task deleted successfully!" : "Task not found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error while deleting task.";
        }
    }

    public List<TaskData> getTasks(Long chatId, String status) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        return database.getTasks(chatId, status);
    }

    public void updateTaskNotificationCount(int taskId, int newCount) {
        database.updateTaskNotificationCount(taskId, newCount);
    }

    // Новый метод для обновления времени последнего уведомления
    public void updateTaskLastNotifyTime(int taskId, Timestamp time) {
        database.updateTaskLastNotificationTime(taskId, time);
    }
}
