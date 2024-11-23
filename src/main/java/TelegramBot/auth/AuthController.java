package TelegramBot.auth;

import TelegramBot.model.UserRepository;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import java.sql.SQLException;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public SendMessage registerCommand(Long chatId) {
        String message = authService.registerUser(chatId);
        return new SendMessage(String.valueOf(chatId), message);
    }

    public boolean isUserRegistered(Long chatId) throws SQLException {
        return authService.getUserByChatId(chatId) != null;
    }
}
