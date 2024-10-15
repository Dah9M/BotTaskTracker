package TelegramBot.Bot.authentification;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public SendMessage registerCommand(Long chatId, String username, String password) {
        String message = authService.registerUser(chatId, username, password);

        return new SendMessage(String.valueOf(chatId), message);
    }

    public SendMessage authorizeCommand(Long chatId, String username, String password) {
        String message = authService.authorizeUser(chatId, username, password);

        return new SendMessage(String.valueOf(chatId), message);
    }
}