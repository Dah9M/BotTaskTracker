package TelegramBot.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TaskRepository {
    private final DatabaseConnector database;

    public TaskRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean addTask(Task task) throws SQLException {
        String query = "INSERT INTO tasks (chat_id, description, deadline, priority, status, creation_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, task.getChatId());
            statement.setString(2, task.getDescription());
            statement.setTimestamp(3, task.getDeadline() != null ? task.getDeadline() : null);
            statement.setString(4, task.getPriority());
            statement.setString(5, task.getStatus());
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            return statement.executeUpdate() > 0;
        }
    }
}
