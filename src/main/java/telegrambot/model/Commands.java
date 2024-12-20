package telegrambot.model;

import telegrambot.auth.AuthController;
import telegrambot.service.Keyboard;
import telegrambot.service.MessageSender;
import telegrambot.task.TaskController;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Commands {
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
                    messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Welcome back! Here is your main menu:");
                    log.info("Пользователь {} вернулся и получил главное меню.", messageSender.getCurrentChatId());
                } else {
                    messageSender.sendReplyMarkup(keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
                    log.info("Пользователь {} не зарегистрирован и получил меню регистрации.", messageSender.getCurrentChatId());
                }
            } catch (SQLException e) {
                log.error("Ошибка при проверке регистрации пользователя: {}", messageSender.getCurrentChatId(), e);
                messageSender.sendMessage("An error occurred while checking registration status.");
            }
        });

        commandMap.put("/menu", () ->
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Menu"));

        commandMap.put("Add Task", () -> {
            String response = taskController.addTaskCommand();
            messageSender.sendMessage(response);
            log.info("Пользователь {} инициировал добавление задачи.", messageSender.getCurrentChatId());
        });

        // Callback команды
        commandMap.put("Register", () -> {
            String registrationMessage = authController.registerCommand(messageSender.getCurrentChatId()).getText();

            messageSender.sendMessage(registrationMessage);
            log.info("Пользователь {} получил сообщение о регистрации: {}", messageSender.getCurrentChatId(), registrationMessage);

            if ("Registration successful!".equals(registrationMessage)) {
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Here is your main menu:");
                log.info("Пользователь {} успешно зарегистрирован и получил главное меню.", messageSender.getCurrentChatId());
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
                taskController.viewTasksCommand("WORK"));
        commandMap.put("Life", () ->
                taskController.viewTasksCommand("LIFE"));
        commandMap.put("Education", () ->
                taskController.viewTasksCommand("EDUCATION"));

        // По приоритету
        commandMap.put("Low", () ->
                taskController.viewTasksCommand("Low"));
        commandMap.put("Medium", () ->
                taskController.viewTasksCommand("Medium"));
        commandMap.put("High", () ->
                taskController.viewTasksCommand("High"));

        commandMap.put("Update Task", () -> {
            if (taskController.hasTasks()) {
                taskController.viewTasksCommand("allTasks");
                String response = taskController.updateTaskCommand();
                messageSender.sendMessage(response);
                log.info("Пользователь {} начал процесс обновления задачи.", messageSender.getCurrentChatId());
            } else {
                messageSender.sendMessage("No tasks found.");
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
                log.warn("Пользователь {} попытался обновить задачу, но задач нет.", messageSender.getCurrentChatId());
            }
        });

        commandMap.put("Delete Task", () -> {
            if (taskController.hasTasks()) {
                String response = taskController.deleteTaskCommand();
                messageSender.sendMessage(response);
                log.info("Пользователь {} начал процесс удаления задачи.", messageSender.getCurrentChatId());
            } else {
                messageSender.sendMessage("No tasks found.");
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
                log.warn("Пользователь {} попытался удалить задачу, но задач нет.", messageSender.getCurrentChatId());
            }
        });

        commandMap.put("Help", () -> {
            String helpMessage = "Here is how you can use the bot:\n" +
                    "- Add Task: Add a new task.\n" +
                    "- Update Task: Update an existing task.\n" +
                    "- Delete Task: Remove a task.\n" +
                    "- View Tasks: View all your tasks.";
            messageSender.sendMessage(helpMessage);
            messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Main menu:");
            log.info("Пользователь {} запросил помощь.", messageSender.getCurrentChatId());
        });

    }

    public static Commands getInstance(BotUtils botUtils) {
        if (instance == null) {
            synchronized (Commands.class) {
                if (instance == null) {
                    instance = new Commands(botUtils);
                    log.info("Экземпляр Commands создан.");
                }
            }
        }
        return instance;
    }

    public Runnable getCommand(String command) {
        return commandMap.get(command);
    }
}
