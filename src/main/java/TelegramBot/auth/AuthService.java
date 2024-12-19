package TelegramBot.auth;

import TelegramBot.model.User;
import TelegramBot.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository database;

    public AuthService(UserRepository database) {
        this.database = database;
    }

    public String registerUser(Long chatId) {
        try {
            if (database.getUserByChatId(chatId) != null) {
                logger.info("Пользователь {} уже зарегистрирован.", chatId);
                return "You've already registered.";
            }

            User user = new User(chatId);
            boolean success = database.registerUser(user);
            if (success) {
                logger.info("Пользователь {} успешно зарегистрирован.", chatId);
                return "Registration successful!";
            } else {
                logger.warn("Не удалось зарегистрировать пользователя {}.", chatId);
                return "Registration failed.";
            }

        } catch (SQLException e) {
            logger.error("Ошибка при регистрации пользователя {}.", chatId, e);
            return "Registration error.";
        }
    }

    // Метод для получения пользователя по chatId
    public User getUserByChatId(Long chatId) throws SQLException {
        try {
            User user = database.getUserByChatId(chatId);
            if (user != null) {
                logger.debug("Пользователь {} найден.", chatId);
            } else {
                logger.debug("Пользователь {} не найден.", chatId);
            }
            return user;
        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователя по chatId: {}", chatId, e);
            throw e;
        }
    }
}
