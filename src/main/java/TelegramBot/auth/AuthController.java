package TelegramBot.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.sql.SQLException;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public SendMessage registerCommand(Long chatId) {
        String message = authService.registerUser(chatId);
        logger.info("Пользователь {} зарегистрирован: {}", chatId, message);
        return new SendMessage(String.valueOf(chatId), message);
    }

    public boolean isUserRegistered(Long chatId) throws SQLException {
        try {
            boolean registered = authService.getUserByChatId(chatId) != null;
            logger.debug("Проверка регистрации пользователя {}: {}", chatId, registered);
            return registered;
        } catch (SQLException e) {
            logger.error("Ошибка при проверке регистрации пользователя {}", chatId, e);
            throw e;
        }
    }
}
