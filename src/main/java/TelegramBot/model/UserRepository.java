package TelegramBot.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseConnector database;

    public UserRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean registerUser(User user) throws SQLException {
        String query = "INSERT INTO users (chat_id) VALUES (?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.getChatId());
            return statement.executeUpdate() > 0;
        }
    }

    public User getUserByChatId(Long chatId) throws SQLException {
        String query = "SELECT * FROM users WHERE chat_id = ?";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, chatId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                return new User(result.getLong("chat_id"));
            }

            return null;
        }
    }
}

