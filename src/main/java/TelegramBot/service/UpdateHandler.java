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
import java.sql.SQLException;

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
        commandMap.put("/start", () -> {
            try {
                if (authController.isUserRegistered(currentChatId)) {
                    messageSender.sendReplyMarkup(currentChatId, keyboard.setMainKeyboard(), "Welcome back! Here is your main menu:");
                } else {
                    messageSender.sendReplyMarkup(currentChatId, keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                messageSender.sendMessage(currentChatId, "An error occurred while checking registration status.");
            }
        });
        commandMap.put("/menu", () ->
                messageSender.sendReplyMarkup(currentChatId, keyboard.setMainKeyboard(), "Menu"));
        commandMap.put("addTask", () -> {
            String response = taskController.addTaskCommand(currentChatId);
            messageSender.sendMessage(currentChatId, response);
        });

        // Callback команды
        callbackMap.put("register", () -> {
            String registrationMessage = authController.registerCommand(currentChatId).getText();
            messageSender.sendMessage(currentChatId, registrationMessage);

            if (registrationMessage.equals("Registration successful!")) {
                messageSender.sendReplyMarkup(currentChatId, keyboard.setMainKeyboard(), "Here is your main menu:");
            }
        });

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

        callbackMap.put("addTask", () -> {
            String response = taskController.addTaskCommand(currentChatId);
            messageSender.sendMessage(currentChatId, response);
        });

        // Обновление задачи
        callbackMap.put("updateTask", () -> {
            if (taskController.hasTasks(currentChatId)) {
                taskController.viewTasksCommand(currentChatId, "allTasks");
                String response = taskController.updateTaskCommand(currentChatId);
                messageSender.sendMessage(currentChatId, response);
            } else {
                messageSender.sendMessage(currentChatId, "No tasks found.");
            }
        });

        // Удаление задачи
        callbackMap.put("deleteTask", () -> {
            if (taskController.hasTasks(currentChatId)) {
                String response = taskController.deleteTaskCommand(currentChatId);
                messageSender.sendMessage(currentChatId, response);
            } else {
                messageSender.sendMessage(currentChatId, "No tasks found.");
            }
        });

        callbackMap.put("help", () -> {
            String helpMessage = "Here is how you can use the bot:\n" +
                    "- Add Task: Add a new task.\n" +
                    "- Update Task: Update an existing task.\n" +
                    "- Delete Task: Remove a task.\n" +
                    "- View Tasks: View all your tasks.";
            messageSender.sendMessage(currentChatId, helpMessage);
        });
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
            String response = taskController.handleTaskInput(currentChatId, currentInput);
            messageSender.sendMessage(currentChatId, response);
        } else if (taskController.isUpdateInProgress(currentChatId)) {
            String response = taskController.handleUpdateInput(currentChatId, currentInput);
            messageSender.sendMessage(currentChatId, response);
        } else if (taskController.isDeleteInProgress(currentChatId)) {
            String response = taskController.handleDeleteInput(currentChatId, currentInput);
            messageSender.sendMessage(currentChatId, response);
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
