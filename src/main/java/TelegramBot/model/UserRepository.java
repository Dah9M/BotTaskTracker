package TelegramBot.model;

import TelegramBot.utils.LoggerFactoryUtil;

import java.util.List; // Импорт списка
import java.util.ArrayList;

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
        } catch (SQLException e) {
            LoggerFactoryUtil.logError("Ошибка при попытке регистрации пользователя: {}", e, user.getChatId());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long chatId = resultSet.getLong("chat_id");
                users.add(new User(chatId));
            }
        } catch (SQLException e) {
            LoggerFactoryUtil.logError("Ошибка при получении всех пользователей: {}", e);
        }

        return users;
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
        } catch (SQLException e) {
            LoggerFactoryUtil.logError("Ошибка при получении по chatId пользователя: {}", e, chatId);
        }
        return null;
    }
}


