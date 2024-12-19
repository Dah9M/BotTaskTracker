package TelegramBot.model;

import TelegramBot.auth.AuthController;
import TelegramBot.service.Keyboard;
import TelegramBot.service.MessageSender;
import TelegramBot.task.TaskController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Commands {
    private static final Logger logger = LoggerFactory.getLogger(Commands.class);
    private static Commands instance;
    private final Map<String, Runnable> commandMap;

    private Commands(BotUtils botUtils) {
        commandMap = new HashMap<>();
        MessageSender messageSender = botUtils.getMessageSender();
        Keyboard keyboard = botUtils.getKeyboard();
        TaskController taskController = botUtils.getTaskController();
        AuthController authController = botUtils.getAuthController();

        commandMap.put("/start", () -> {
            try {
                if (authController.isUserRegistered(messageSender.getCurrentChatId())) {
                    // Пользователь зареган, даем меню
                    messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Welcome back! Here is your main menu:");
                    logger.info("Пользователь {} вернулся и получил главное меню.", messageSender.getCurrentChatId());
                } else {
                    // Пользователь не зареган, даем регистрацию
                    messageSender.sendReplyMarkup(keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
                    logger.info("Пользователь {} не зарегистрирован и получил меню регистрации.", messageSender.getCurrentChatId());
                }
            } catch (SQLException e) {
                logger.error("Ошибка при проверке регистрации пользователя: {}", messageSender.getCurrentChatId(), e);
                messageSender.sendMessage("An error occurred while checking registration status.");
            }
        });

        commandMap.put("/menu", () ->
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Menu"));

        commandMap.put("Add Task", () -> {
            String response = taskController.addTaskCommand();
            messageSender.sendMessage(response);
            logger.info("Пользователь {} инициировал добавление задачи.", messageSender.getCurrentChatId());
        });

        // Callback команды
        commandMap.put("Register", () -> {
            // Результат регистрации
            String registrationMessage = authController.registerCommand(messageSender.getCurrentChatId()).getText();

            // Сообщение с регистрацией
            messageSender.sendMessage(registrationMessage);
            logger.info("Пользователь {} получил сообщение о регистрации: {}", messageSender.getCurrentChatId(), registrationMessage);

            // Если успешная регистрация, даем меню
            if ("Registration successful!".equals(registrationMessage)) {
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Here is your main menu:");
                logger.info("Пользователь {} успешно зарегистрирован и получил главное меню.", messageSender.getCurrentChatId());
            }
        });

        commandMap.put("View Tasks", () ->
                messageSender.sendReplyMarkup(keyboard.setViewTasksKeyboard(), "Select:"));

        commandMap.put("By Status", () ->
                messageSender.sendReplyMarkup(keyboard.setStatusKeyboard(), "Select Status:"));
        commandMap.put("By Priority", () ->
                messageSender.sendReplyMarkup(keyboard.setPriorityKeyboard(), "Select Priority:"));
        commandMap.put("By Category", () ->
                messageSender.sendReplyMarkup(keyboard.setCategoryKeyboard(), "Select Category:"));
        commandMap.put("All", () ->
                taskController.viewTasksCommand("allTasks"));

        // По статусу
        commandMap.put("All By Status", () ->
                taskController.viewTasksCommand("allTasks"));
        commandMap.put("Waiting", () ->
                taskController.viewTasksCommand("Waiting"));
        commandMap.put("Active", () ->
                taskController.viewTasksCommand("Active"));
        commandMap.put("Completed", () ->
                taskController.viewTasksCommand("Completed"));

        // По категории
        commandMap.put("Work", () ->
                taskController.viewTasksByCategory("WORK"));
        commandMap.put("Life", () ->
                taskController.viewTasksByCategory("LIFE"));
        commandMap.put("Education", () ->
                taskController.viewTasksByCategory("EDUCATION"));
        commandMap.put("All", () ->
                taskController.viewTasksByCategory("all"));

        // По приоритету
        commandMap.put("Low", () ->
                taskController.viewTasksByPriority("Low"));
        commandMap.put("Medium", () ->
                taskController.viewTasksByPriority("Medium"));
        commandMap.put("High", () ->
                taskController.viewTasksByPriority("High"));
        commandMap.put("All", () ->
                taskController.viewTasksByPriority("All"));

        // Обновление задачи (одну! выбранную!)
        commandMap.put("Update Task", () -> {
            // Проверка есть ли задачи у пользователя
            if (taskController.hasTasks()) {
                taskController.viewTasksCommand("allTasks");
                String response = taskController.updateTaskCommand();
                messageSender.sendMessage(response);
                logger.info("Пользователь {} начал процесс обновления задачи.", messageSender.getCurrentChatId());
            } else {
                // Задач нет
                messageSender.sendMessage("No tasks found.");
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
                logger.warn("Пользователь {} попытался обновить задачу, но задач нет.", messageSender.getCurrentChatId());
            }
        });

        // Удаление задачи
        commandMap.put("Delete Task", () -> {
            // Проверка, есть ли задачи у пользователя
            if (taskController.hasTasks()) {
                String response = taskController.deleteTaskCommand();
                messageSender.sendMessage(response);
                logger.info("Пользователь {} начал процесс удаления задачи.", messageSender.getCurrentChatId());
            } else {
                // Задач нет
                messageSender.sendMessage("No tasks found.");
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
                logger.warn("Пользователь {} попытался удалить задачу, но задач нет.", messageSender.getCurrentChatId());
            }
        });

        // Помощь
        commandMap.put("Help", () -> {
            String helpMessage = "Here is how you can use the bot:\n" +
                    "- Add Task: Add a new task.\n" +
                    "- Update Task: Update an existing task.\n" +
                    "- Delete Task: Remove a task.\n" +
                    "- View Tasks: View all your tasks.";
            messageSender.sendMessage(helpMessage);
            messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
            logger.info("Пользователь {} запросил помощь.", messageSender.getCurrentChatId());
        });
    }

    public static Commands getInstance(BotUtils botUtils) {
        if (instance == null) {
            synchronized (Commands.class) {
                if (instance == null) {
                    instance = new Commands(botUtils);
                    logger.info("Экземпляр Commands создан.");
                }
            }
        }
        return instance;
    }

    public Runnable getCommand(String command) {
        return commandMap.get(command);
    }
}
