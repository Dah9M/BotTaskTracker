package TelegramBot.model;

import TelegramBot.service.Keyboard;
import TelegramBot.service.MessageSender;
import TelegramBot.task.TaskController;
import TelegramBot.utils.LoggerFactoryUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Commands {
    private static Commands instance;
    private final Map<String, Runnable> commandMap;

    private Commands(BotUtils botUtils) {
        commandMap = new HashMap<>();
        MessageSender messageSender = botUtils.getMessageSender();
        Keyboard keyboard = botUtils.getKeyboard();
        TaskController taskController = botUtils.getTaskController();

        commandMap.put("/start", () -> {
            try {
                if (botUtils.getAuthController().isUserRegistered(botUtils.getMessageSender().getCurrentChatId())) {
                    // пользователь зареган, даем меню
                    messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Welcome back! Here is your main menu:");
                } else {
                    // пользователь не зареган, даем регу
                    messageSender.sendReplyMarkup(keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
                }
            } catch (SQLException e) {
                LoggerFactoryUtil.logError("Ошибка при проверке регистрации пользователя: {}", e, botUtils.getMessageSender().getCurrentChatId());

                messageSender.sendMessage("An error occurred while checking registration status.");
            }
        });
        commandMap.put("/menu", () ->
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Menu"));
        commandMap.put("Add Task", () -> {
            String response = taskController.addTaskCommand();
            messageSender.sendMessage(response);
        });

        // Callback команды
        commandMap.put("Register", () -> {
            // результат реги
            String registrationMessage = botUtils.getAuthController().registerCommand(botUtils.getMessageSender().getCurrentChatId()).getText();

            // сообщение с регой
            messageSender.sendMessage(registrationMessage);

            // если успешная рега, даем менюшку
            if (registrationMessage.equals("Registration successful!")) {
                messageSender.sendReplyMarkup(keyboard.setMainKeyboard(), "Here is your main menu:");
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

        // жоска обновляем таску (одну! выбранную!)
        commandMap.put("Update Task", () -> {
            // проверка есть ли таски у типочка
            if (taskController.hasTasks()) {
                taskController.viewTasksCommand("allTasks");
                String response = taskController.updateTaskCommand();
                messageSender.sendMessage(response);
            } else {
                // тасок нет, пошел он на... со своими исправлениями
                messageSender.sendMessage("No tasks found.");
                botUtils.getKeyboard().setMainKeyboard();
            }
        });

        // удаление таски
        commandMap.put("Delete Task", () -> {
            // проверка, есть ли таски у типочка
            if (taskController.hasTasks()) {
                String response = taskController.deleteTaskCommand();
                messageSender.sendMessage(response);
            } else {
                // тасок нет, пошел он на... со своими удалениями
                messageSender.sendMessage("No tasks found.");
                botUtils.getKeyboard().setMainKeyboard();
            }
        });


        // помощь (тебе, немощному)
        commandMap.put("Help", () -> {
            String helpMessage = "Here is how you can use the bot:\n" +
                    "- Add Task: Add a new task.\n" +
                    "- Update Task: Update an existing task.\n" +
                    "- Delete Task: Remove a task.\n" +
                    "- View Tasks: View all your tasks.";
            messageSender.sendMessage(helpMessage);
            botUtils.getKeyboard().setMainKeyboard();
        });

    }

    public static Commands getInstance(BotUtils botUtils) {
        if (instance == null) {
            synchronized (Commands.class) {
                if (instance == null) {
                    instance = new Commands(botUtils);
                }
            }
        }
        return instance;
    }

    public Runnable getCommand(String command) {
        return commandMap.get(command);
    }
}
