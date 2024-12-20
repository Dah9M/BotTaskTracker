package telegrambot.auth;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.sql.SQLException;

@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public SendMessage registerCommand(@NonNull Long chatId) {
        String message = authService.registerUser(chatId);
        log.info("Пользователь {} зарегистрирован: {}", chatId, message);
        return new SendMessage(String.valueOf(chatId), message);
    }

    public boolean isUserRegistered(@NonNull Long chatId) throws SQLException {
        try {
            boolean registered = authService.getUserByChatId(chatId) != null;
            log.debug("Проверка регистрации пользователя {}: {}", chatId, registered);
            return registered;
        } catch (SQLException e) {
            log.error("Ошибка при проверке регистрации пользователя {}", chatId, e);
            throw e;
        }
    }
}
