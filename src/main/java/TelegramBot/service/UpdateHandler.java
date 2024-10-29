package TelegramBot.service;

import TelegramBot.Bot.authentification.AuthController;
import TelegramBot.model.DatabaseConnector;
import TelegramBot.model.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import TelegramBot.Bot.authentification.AuthService;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);
        DatabaseConnector databaseConnector = new DatabaseConnector();
        UserRepository userRepository = new UserRepository(databaseConnector);
        AuthService authService = new AuthService(userRepository);
        this.authController = new AuthController(authService);
    }

    public void updateHandle(Update update) {
        long chatId = 0;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(), "Добро пожаловать! Пожалуйста, зарегистрируйтесь, нажав кнопку ниже.");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("register")) {
                String response = authController.registerCommand(chatId).getText();
                messageSender.sendMessage(chatId, response);
            }
        }
    }
}
