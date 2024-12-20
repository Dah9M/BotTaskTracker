package telegrambot.auth;

import lombok.NonNull;
import telegrambot.model.User;
import telegrambot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class AuthService {
    private final UserRepository database;

    public AuthService(UserRepository database) {
        this.database = database;
    }

    public String registerUser(@NonNull Long chatId) {
        try {
            if (database.getUserByChatId(chatId) != null) {
                log.info("Пользователь {} уже зарегистрирован.", chatId);
                return "You've already registered.";
            }

            User user = new User(chatId);
            boolean success = database.registerUser(user);
            if (success) {
                log.info("Пользователь {} успешно зарегистрирован.", chatId);
                return "Registration successful!";
            } else {
                log.warn("Не удалось зарегистрировать пользователя {}.", chatId);
                return "Registration failed.";
            }

        } catch (SQLException e) {
            log.error("Ошибка при регистрации пользователя {}.", chatId, e);
            return "Registration error.";
        }
    }

    public User getUserByChatId(@NonNull Long chatId) throws SQLException {
        try {
            User user = database.getUserByChatId(chatId);
            if (user != null) {
                log.debug("Пользователь {} найден.", chatId);
            } else {
                log.debug("Пользователь {} не найден.", chatId);
            }
            return user;
        } catch (SQLException e) {
            log.error("Ошибка при получении пользователя по chatId: {}", chatId, e);
            throw e;
        }
    }
}
