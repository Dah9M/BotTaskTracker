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

import java.util.HashMap;
import java.util.Map;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;
    private final TaskController taskController;

    // Мапы для хранения команд
    private final Map<String, Runnable> commandMap = new HashMap<>();
    private final Map<String, Runnable> callbackMap = new HashMap<>();

    private Long currentChatId;
    private String currentInput;

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);

        // Инициализация зависимостей
        DatabaseConnector databaseConnector = new DatabaseConnector();
        UserRepository userRepository = new UserRepository(databaseConnector);
        TaskRepository taskRepository = new TaskRepository(databaseConnector);
        AuthService authService = new AuthService(userRepository);
        TaskService taskService = new TaskService(taskRepository);

        this.authController = new AuthController(authService);
        this.taskController = new TaskController(taskService, messageSender);

        // Инициализация команд
        initializeCommands();
    }

    private void initializeCommands() {
        // Команды текстовых сообщений
        commandMap.put("/start", () ->
                messageSender.sendReplyMarkup(currentChatId, keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below."));
        commandMap.put("/menu", () ->
                messageSender.sendReplyMarkup(currentChatId, keyboard.setMainKeyboard(), "Menu"));
        commandMap.put("addTask", () ->
                messageSender.sendMessage(currentChatId, taskController.addTaskCommand(currentChatId)));
        commandMap.put("updateTask", () -> {
            taskController.viewTasksCommand(currentChatId, "allTasks");
            messageSender.sendMessage(currentChatId, taskController.updateTaskCommand(currentChatId));
        });

        // Callback команды
        callbackMap.put("register", () ->
                messageSender.sendMessage(currentChatId, authController.registerCommand(currentChatId).getText()));
        callbackMap.put("viewTasks", () ->
                messageSender.sendReplyMarkup(currentChatId, keyboard.setViewTasksKeyboard(), "Tasks:"));
        callbackMap.put("allTasks", () ->
                taskController.viewTasksCommand(currentChatId, "allTasks"));
        callbackMap.put("waitingTasks", () ->
                taskController.viewTasksCommand(currentChatId, "waitingTasks"));
        callbackMap.put("activeTasks", () ->
                taskController.viewTasksCommand(currentChatId, "activeTasks"));
        callbackMap.put("completedTasks", () ->
                taskController.viewTasksCommand(currentChatId, "completedTasks"));
    }

    public void updateHandle(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) {
        currentChatId = update.getMessage().getChatId();
        currentInput = update.getMessage().getText();

        if (taskController.isTaskInProgress(currentChatId)) {
            messageSender.sendMessage(currentChatId, taskController.handleTaskInput(currentChatId, currentInput));
        } else if (taskController.isUpdateInProgress(currentChatId)) {
            messageSender.sendMessage(currentChatId, taskController.handleUpdateInput(currentChatId, currentInput));
        } else {
            Runnable command = commandMap.get(currentInput);
            if (command != null) {
                command.run();
            } else {
                messageSender.sendMessage(currentChatId, "Unknown command.");
            }
        }
    }

    private void handleCallbackQuery(Update update) {
        currentChatId = update.getCallbackQuery().getMessage().getChatId();
        currentInput = update.getCallbackQuery().getData();

        Runnable callbackCommand = callbackMap.get(currentInput);
        if (callbackCommand != null) {
            callbackCommand.run();
        } else {
            messageSender.sendMessage(currentChatId, "Unknown command.");
        }
    }
}
