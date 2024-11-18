package TelegramBot.service;

import TelegramBot.Bot.authentification.AuthController;
import TelegramBot.model.DatabaseConnector;
import TelegramBot.model.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import TelegramBot.Bot.authentification.AuthService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;
    private final TaskBuilder taskBuilder = new TaskBuilder();
    private final Map<String, Consumer<Long>> callbackActions = new HashMap<>();

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);

        // Инициализация с использованием синглтонов
        DatabaseConnector databaseConnector = DatabaseConnector.getInstance();
        UserRepository userRepository = UserRepository.getInstance(databaseConnector);
        AuthService authService = AuthService.getInstance(userRepository);
        this.authController = new AuthController(authService);

        // Инициализация callbackActions
        callbackActions.put("register", this::handleRegister);
        callbackActions.put("task1", this::handleTask1);
        callbackActions.put("task2", this::handleTask2);
        // Добавьте другие callbackData и соответствующие методы
    }

    public void updateHandle(Update update) {
        long chatId = 0;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(),
                        "Добро пожаловать! Пожалуйста, зарегистрируйтесь, нажав кнопку ниже.");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            chatId = update.getCallbackQuery().getMessage().getChatId();

            Consumer<Long> action = callbackActions.get(callbackData);
            if (action != null) {
                action.accept(chatId);
            } else {
                messageSender.sendMessage(chatId, "Неизвестная команда.");
            }
        }
    }

    private void handleRegister(Long chatId) {
        String response = authController.registerCommand(chatId).getText();
        messageSender.sendMessage(chatId, response);
    }

    private void handleTask1(Long chatId) {
        String taskData = taskBuilder.getTaskData("task1");
        messageSender.sendMessage(chatId, taskData);
    }

    private void handleTask2(Long chatId) {
        String taskData = taskBuilder.getTaskData("task2");
        messageSender.sendMessage(chatId, taskData);
    }

}
