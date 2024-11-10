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

    public String addTask(Long chatId, String description, Timestamp deadline, String priority, Timestamp creation_date) {
        Task task = new Task(chatId, description, deadline, priority, creation_date);

        try {
            database.addTask(task);
            return "Task added";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error with adding task";
        }
    }

    public List<TaskData> viewTasks(Long chatId, String key) {
        return database.getTasks(chatId, key);
    }

    public String updateTaskField(Long taskId, String field, String newValue) {
        switch (field) {
            case "description":
                return database.updateTaskField(taskId, field, newValue);
            case "deadline":
                try {
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    return database.updateTaskField(taskId, field, deadline);
                } catch (IllegalArgumentException e) {
                    return "Invalid deadline format. Use YYYY-MM-DD HH:MM:SS.";
                }
            case "priority":
                return database.updateTaskField(taskId, field, newValue);
            default:
                return "Invalid field.";
        }
    }
}
