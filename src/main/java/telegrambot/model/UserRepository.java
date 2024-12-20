package telegrambot.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final DatabaseConnector database;

    public UserRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean registerUser(User user) throws SQLException {
        String query = "INSERT INTO users (chat_id) VALUES (?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, user.getChatId());
            boolean success = statement.executeUpdate() > 0;
            if (success) {
                logger.info("Пользователь {} успешно зарегистрирован.", user.getChatId());
            } else {
                logger.warn("Не удалось зарегистрировать пользователя {}.", user.getChatId());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Ошибка при попытке регистрации пользователя: {}", user.getChatId(), e);
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
            logger.info("Получено {} пользователей из базы данных.", users.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении всех пользователей.", e);
        }

        return users;
    }

    public User getUserByChatId(Long chatId) throws SQLException {
        String query = "SELECT * FROM users WHERE chat_id = ?";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, chatId);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                logger.info("Пользователь {} найден в базе данных.", chatId);
                return new User(result.getLong("chat_id"));
            }

            logger.warn("Пользователь {} не найден в базе данных.", chatId);
            return null;
        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователя по chatId: {}", chatId, e);
            throw e;
        }
    }
}
