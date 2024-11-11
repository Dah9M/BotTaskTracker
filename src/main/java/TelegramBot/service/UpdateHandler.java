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
                messageSender.sendMessage(chatId, taskController.handleTaskInput(chatId, messageText));
            } else if (taskController.isUpdateInProgress(chatId)) {
                messageSender.sendMessage(chatId, taskController.handleUpdateInput(chatId, messageText));
            } else if (messageText.equals("/start")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
            } else if (messageText.equals("/menu")) {
                messageSender.sendReplyMarkup(chatId, keyboard.setMainKeyboard(), "Menu");
            }

        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callbackData) {
                case "register":
                    messageSender.sendMessage(chatId, authController.registerCommand(chatId).getText());
                    break;
                case "addTask":
                    messageSender.sendMessage(chatId, taskController.addTaskCommand(chatId));
                    break;
                case "viewTasks":
                    messageSender.sendReplyMarkup(chatId, keyboard.setViewTasksKeyboard(), "Tasks:");
                    break;
                case "allTasks":
                case "waitingTasks":
                case "activeTasks":
                case "completedTasks":
                    taskController.viewTasksCommand(chatId, callbackData);
                    break;
                case "updateTask":
                    taskController.viewTasksCommand(chatId, "allTasks");
                    taskController.updateTaskCommand(chatId);
                    break;
                default:
                    messageSender.sendMessage(chatId, "Unknown command.");
                    break;
            }
        }
    }
}
