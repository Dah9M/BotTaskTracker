package TelegramBot.auth;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public SendMessage registerCommand(Long chatId) {
        String message = authService.registerUser(chatId);
        return new SendMessage(String.valueOf(chatId), message);
    }
}
