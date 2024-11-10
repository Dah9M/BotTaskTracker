package TelegramBot.service;

import TelegramBot.auth.AuthController;
import TelegramBot.model.DatabaseConnector;
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
        this.taskController = new TaskController(taskService, messageSender);
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
                messageSender.sendMessage(chatId, authController.registerCommand(chatId).getText());
            } else if (callbackData.equals("addTask")) {
                messageSender.sendMessage(chatId, taskController.addTaskCommand(chatId));
            } else if (callbackData.equals("viewTasks")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setViewTasksKeyboard(), "Tasks:");
            } else if (callbackData.equals("allTasks") || callbackData.equals("waitingTasks") || callbackData.equals("activeTasks") || callbackData.equals("completedTasks")) {
                taskController.viewTasksCommand(chatId, callbackData);
            }
        }
    }
}
