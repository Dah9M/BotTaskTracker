package telegrambot.model;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UserRepository {
    private final DatabaseConnector database;

    public UserRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean registerUser(@NonNull User user) throws SQLException {
        String query = "INSERT INTO users (chat_id) VALUES (?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.getChatId());
            boolean success = statement.executeUpdate() > 0;
            if (success) {
                log.info("Пользователь {} успешно зарегистрирован.", user.getChatId());
            } else {
                log.warn("Не удалось зарегистрировать пользователя {}.", user.getChatId());
            }
            return success;
        } catch (SQLException e) {
            log.error("Ошибка при попытке регистрации пользователя: {}", user.getChatId(), e);
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
            log.info("Получено {} пользователей из базы данных.", users.size());
        } catch (SQLException e) {
            log.error("Ошибка при получении всех пользователей.", e);
        }

        return users;
    }

    public User getUserByChatId(@NonNull Long chatId) throws SQLException {
        String query = "SELECT * FROM users WHERE chat_id = ?";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, chatId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                log.info("Пользователь {} найден в базе данных.", chatId);
                return new User(result.getLong("chat_id"));
            }

            log.warn("Пользователь {} не найден в базе данных.", chatId);
            return null;
        } catch (SQLException e) {
            log.error("Ошибка при получении пользователя по chatId: {}", chatId, e);
            throw e;
        }
    }
}
