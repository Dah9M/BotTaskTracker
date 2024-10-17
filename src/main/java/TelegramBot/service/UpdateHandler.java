package TelegramBot.service;

import TelegramBot.Bot.authentification.AuthController;
import TelegramBot.model.DatabaseConnector;
import TelegramBot.model.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import TelegramBot.Bot.authentification.AuthService;

import java.util.HashMap;
import java.util.Map;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;
    private final Map<Long, String> userStates = new HashMap<>();

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);
        DatabaseConnector databaseConnector = new DatabaseConnector();
        UserRepository userRepository = new UserRepository(databaseConnector);
        AuthService authService = new AuthService(userRepository);
        this.authController = new AuthController(authService);
    }

    public void updateHandle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (userStates.containsKey(chatId)) {
                String currentState = userStates.get(chatId);

                if (currentState.equals("AWAITING_REGISTRATION")) {
                    handleRegistration(chatId, messageText);
                } else if (currentState.equals("AWAITING_LOGIN")) {
                    handleLogin(chatId, messageText);
                }
                return;
            }

            String[] commandArgs = messageText.split(" ");
            String command = commandArgs[0];

            if (command.equals("/start")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(), "Выберите действие:");
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("register")) {
                messageSender.sendMessage(chatId, "Введите данные для регистрации в формате: <username> <password>");
                userStates.put(chatId, "AWAITING_REGISTRATION");
            } else if (callbackData.equals("auth")) {
                messageSender.sendMessage(chatId, "Введите пароль для авторизации:");
                userStates.put(chatId, "AWAITING_LOGIN");
            }
        }
    }

    private void handleRegistration(long chatId, String messageText) {
        String[] parts = messageText.split(" ");
        if (parts.length < 2) {
            messageSender.sendMessage(chatId, "Неверный формат. Пожалуйста, введите логин и пароль через пробел.");
            return;
        }

        String username = parts[0];
        String password = parts[1];
        String response = authController.registerCommand(chatId, username, password).getText();
        messageSender.sendMessage(chatId, response);

        userStates.remove(chatId);
    }

    private void handleLogin(long chatId, String password) {
        String response = authController.authorizeCommand(chatId, password).getText();
        messageSender.sendMessage(chatId, response);
        userStates.remove(chatId);
    }
}
