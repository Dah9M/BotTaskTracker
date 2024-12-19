package TelegramBot.model;

import TelegramBot.task.TaskData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private static final Logger logger = LoggerFactory.getLogger(TaskRepository.class);
    private final DatabaseConnector database;

    public TaskRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean addTask(Task task) throws SQLException {
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
            statement.setString(9, task.getCategory() != null ? task.getCategory().name() : null);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении задачи пользователя: {}", task.getChatId(), e);
            return false;
        }
    }

    public List<TaskData> getTasks(Long chatId, String key) {
        if (chatId == null) {
            logger.error("Ошибка при получении задач пользователя, chatId = null");
            throw new IllegalArgumentException("Chat ID cannot be null");
        }

        List<TaskData> tasks = new ArrayList<>();
        String query;

        switch (key) {
            case "byCategory":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND category = ?";
                break;
            case "Waiting":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Waiting'";
                break;
            case "Active":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Active'";
                break;
            case "Completed":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Completed'";
                break;
            case "allTasks":
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
                    int deadlineNotificationCount = resultSet.getInt("deadline_notification_count");

                    TaskData task = new TaskData(dbID, chatid, description, deadline, TaskPriority.valueOf(priority.toUpperCase()), status, creationDate, deadlineNotificationCount);
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении задач пользователя: {}", chatId, e);
        }

        return tasks;
    }

    public List<TaskData> getTasksByCategory(Long chatId, String category) {
        List<TaskData> tasks = new ArrayList<>();
        String query = "SELECT * FROM tasks WHERE chat_id = ? AND category = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, chatId);
            statement.setString(2, category);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int dbID = resultSet.getInt("id");
                    String description = resultSet.getString("description");
                    Timestamp deadline = resultSet.getTimestamp("deadline");
                    String priority = resultSet.getString("priority");
                    String status = resultSet.getString("status");
                    Timestamp creationDate = resultSet.getTimestamp("creation_date");
                    int notificationCount = resultSet.getInt("deadline_notification_count");

                    tasks.add(new TaskData(dbID, chatId, description, deadline,
                            TaskPriority.valueOf(priority.toUpperCase()), status, creationDate, notificationCount));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении задач по категории пользователя: {}", chatId, e);
        }

        return tasks;
    }

    public List<TaskData> getTasksByPriority(Long chatId, String priority) {
        List<TaskData> tasks = new ArrayList<>();
        String query = "SELECT * FROM tasks WHERE chat_id = ? AND priority = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setLong(1, chatId);
            statement.setString(2, priority);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int dbID = resultSet.getInt("id");
                    String description = resultSet.getString("description");
                    Timestamp deadline = resultSet.getTimestamp("deadline");
                    String taskPriority = resultSet.getString("priority");
                    String status = resultSet.getString("status");
                    Timestamp creationDate = resultSet.getTimestamp("creation_date");
                    int notificationCount = resultSet.getInt("deadline_notification_count");

                    tasks.add(new TaskData(dbID, chatId, description, deadline,
                            TaskPriority.valueOf(taskPriority.toUpperCase()), status, creationDate, notificationCount));
                }
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении задач по приоритету пользователя: {}", chatId, e);
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
            logger.error("Ошибка при обновлении notificationCount задачи: {}", taskId, e);
            return false;
        }
    }

    // Обновление отдельного поля задачи
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
                logger.warn("Unsupported data type for field update: {}", newValue.getClass().getName());
                return "Unsupported data type for field update.";
            }

            statement.setLong(2, id);
            statement.executeUpdate();
            return fieldName + " updated successfully.";
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении задачи: {}", id, e);
            return "Error updating " + fieldName + ".";
        }
    }

    // Удаление задачи по ID
    public boolean deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM tasks WHERE id = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, taskId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении задачи: {}", taskId, e);
            return false;
        }
    }
}
