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

    // Добавление новой задачи в базу данных
    public String addTask(Long chatId, String description, Timestamp deadline, String priority, Timestamp creationDate) {
        Task task = new Task(chatId, description, deadline, priority, creationDate);
        try {
            database.addTask(task);
            return "Task added successfully!";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error while adding task.";
        }
    }

    // Получение задач пользователя с фильтрацией по статусу
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

    // Обновление поля задачи
    public String updateTaskField(Long chatId, int dbID, String field, String newValue) {
        List<TaskData> tasks = database.getTasks(chatId, "allTasks");
        if (dbID < 0 || dbID >= tasks.size()) {
            return "Task ID not found.";
        }

        TaskData task = tasks.get(dbID);
        long trueId = task.getDbID();

        try {
            switch (field) {
                case "description":
                case "priority":
                    return database.updateTaskField(trueId, field, newValue);
                case "deadline":
                    Timestamp deadline = Timestamp.valueOf(newValue);
                    return database.updateTaskField(trueId, field, deadline);
                default:
                    return "Invalid field.";
            }
        } catch (IllegalArgumentException e) {
            return "Invalid deadline format. Use YYYY-MM-DD HH:MM:SS.";
        }
    }

    // Удаление задачи
    public String deleteTask(Long taskId) {
        try {
            boolean success = database.deleteTask(taskId);
            return success ? "Task deleted successfully!" : "Task not found.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error while deleting task.";
        }
    }
}