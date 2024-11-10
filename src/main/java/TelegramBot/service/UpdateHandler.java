package TelegramBot.service;

import TelegramBot.auth.AuthController;
import TelegramBot.model.DatabaseConnector;
import TelegramBot.task.TaskBuilder;
import TelegramBot.model.TaskRepository;
import TelegramBot.model.UserRepository;
import TelegramBot.task.TaskController;
import TelegramBot.task.TaskService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import TelegramBot.auth.AuthService;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;
    private final TaskController taskController;

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);
        DatabaseConnector databaseConnector = new DatabaseConnector();
        UserRepository userRepository = new UserRepository(databaseConnector);
        TaskRepository taskRepository = new TaskRepository(databaseConnector);
        AuthService authService = new AuthService(userRepository);
        TaskService taskService = new TaskService(taskRepository);
        this.authController = new AuthController(authService);
        this.taskController = new TaskController(taskService);
    }

    public void updateHandle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (taskController.isTaskInProgress(chatId)) {
                String response = taskController.handleTaskInput(chatId, messageText);
                messageSender.sendMessage(chatId, response);
            } else if (messageText.equals("/start")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
            } else if (messageText.equals("/menu")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setMainKeyboard(), "Menu");
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("register")) {
                String response = authController.registerCommand(chatId).getText();
                messageSender.sendMessage(chatId, response);
            } else if (callbackData.equals("addTask")) {
                String response = taskController.addTaskCommand(chatId);
                messageSender.sendMessage(chatId, response);
            } else if (callbackData.equals("updateTask")) {

            }
        }
    }
}
