package telegrambot.model;

import lombok.NonNull;
import telegrambot.task.TaskData;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class TaskRepository {
    private final DatabaseConnector database;

    public TaskRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean addTask(@NonNull Task task) throws SQLException {
        String query = "INSERT INTO tasks (chat_id, description, deadline, priority, status, creation_date, notified, deadline_notification_count, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, task.getChatId());
            statement.setString(2, task.getDescription());
            statement.setTimestamp(3, task.getDeadline() != null ? task.getDeadline() : null);
            statement.setString(4, task.getPriority().name());
            statement.setString(5, task.getStatus());
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            statement.setBoolean(7, task.isNotified());
            statement.setInt(8, task.getDeadlineNotificationCount());
            statement.setString(9, task.getCategory().toString());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Ошибка при добавлении задачи пользователя: {}", task.getChatId(), e);
            return false;
        }
    }

    public List<TaskData> getTasks(@NonNull Long chatId, String key) {
        if (chatId == null) {
            log.error("Ошибка при получении задач пользователя, chatId = null");
            throw new IllegalArgumentException("Chat ID cannot be null");
        }

        List<TaskData> tasks = new ArrayList<>();
        String query;

        switch (key) {
            case "Waiting":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Waiting'";
                break;
            case "Active":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Active'";
                break;
            case "Completed":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Completed'";
                break;
            case "LOW":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND priority = 'LOW'";
                break;
            case "MEDIUM":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND priority = 'MEDIUM'";
                break;
            case "HIGH":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND priority = 'HIGH'";
                break;
            case "WORK":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND category = 'WORK'";
                break;
            case "LIFE":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND category = 'LIFE'";
                break;
            case "EDUCATION":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND category = 'EDUCATION'";
                break;
            default:
                query = "SELECT * FROM tasks WHERE chat_id = ?";
                break;
        }

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, chatId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int dbID = resultSet.getInt("id");
                    Long chatid = resultSet.getLong("chat_id");
                    String description = resultSet.getString("description");
                    Timestamp deadline = resultSet.getTimestamp("deadline");
                    String priority = resultSet.getString("priority");
                    String status = resultSet.getString("status");
                    Timestamp creationDate = resultSet.getTimestamp("creation_date");
                    int notificationCount = resultSet.getInt("deadline_notification_count");
                    String categoryName = resultSet.getString("category");

                    tasks.add(new TaskData(dbID, chatId, description, deadline,
                            TaskPriority.valueOf(priority.toUpperCase()), status, creationDate, notificationCount, TaskCategory.valueOf(categoryName)));
                }
            }
        } catch (SQLException e) {
            log.error("Ошибка при получении задач пользователя: {}", chatId, e);
        }

        return tasks;
    }

    public boolean updateTaskNotificationCount(int taskId, int newCount) {
        String query = "UPDATE tasks SET deadline_notification_count = ? WHERE id = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newCount);
            statement.setInt(2, taskId);
            int rowsUpdated = statement.executeUpdate();

            return rowsUpdated > 0;
        } catch (SQLException e) {
            log.error("Ошибка при обновлении notificationCount задачи: {}", taskId, e);
            return false;
        }
    }

    public <T> String updateTaskField(int id, String fieldName, T newValue) {
        String query = "UPDATE tasks SET " + fieldName + " = ? WHERE id = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (newValue instanceof String) {
                statement.setString(1, (String) newValue);
            } else if (newValue instanceof Timestamp) {
                statement.setTimestamp(1, (Timestamp) newValue);
            } else if (newValue instanceof TaskCategory) {
                statement.setString(1, ((TaskCategory) newValue).name());
            } else {
                log.warn("Unsupported data type for field update: {}", newValue.getClass().getName());
                return "Unsupported data type for field update.";
            }

            statement.setLong(2, id);
            statement.executeUpdate();
            return fieldName + " updated successfully.";
        } catch (SQLException e) {
            log.error("Ошибка при обновлении задачи: {}", id, e);
            return "Error updating " + fieldName + ".";
        }
    }

    public boolean deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, taskId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Ошибка при удалении задачи: {}", taskId, e);
            return false;
        }
    }
}
