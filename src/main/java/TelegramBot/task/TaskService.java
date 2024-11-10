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

    public List<Task> viewTasks(Long chatId, String key) {
        return database.getTasks(chatId, key);
    }
}
