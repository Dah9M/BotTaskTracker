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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class UpdateHandler {
    private final Keyboard keyboard = new Keyboard();
    private final MessageSender messageSender;
    private final AuthController authController;
    private final TaskController taskController;

    private final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

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

            if (userState.containsKey(chatId)) {
                handleTaskCreation(chatId, messageText);
            } else {
                if (messageText.equals("/start")) {
                    messageSender.sendReplyMarkup(chatId, keyboard.setStartKeyboard(), "Welcome! Please, register by clicking the button below.");
                } else if (messageText.equals("/menu")) {
                    messageSender.sendReplyMarkup(chatId, keyboard.setMainKeyboard(), "Menu");
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("register")) {
                String response = authController.registerCommand(chatId).getText();
                messageSender.sendMessage(chatId, response);
            } else if (callbackData.equals("addTask")) {
                startTaskCreation(chatId);
            }
        }
    }


    private void startTaskCreation(long chatId) {
        userState.put(chatId, "awaiting_description");
        taskDataMap.put(chatId, new TaskData());
        messageSender.sendMessage(chatId, "Please provide a description for the task.");
    }

    private void handleTaskCreation(long chatId, String messageText) {
        TaskData taskData = taskDataMap.get(chatId);

        switch (userState.get(chatId)) {
            case "awaiting_description":
                taskData.setDescription(messageText);
                userState.put(chatId, "awaiting_deadline");
                messageSender.sendMessage(chatId, "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS).");
                break;

            case "awaiting_deadline":
                taskData.setDeadline(java.sql.Timestamp.valueOf(messageText));
                userState.put(chatId, "awaiting_priority");
                messageSender.sendMessage(chatId, "Please provide a priority for the task (e.g., High, Medium, Low).");
                break;

            case "awaiting_priority":
                taskData.setPriority(messageText);
                taskData.setChatId(chatId);
                taskData.setStatus("Pending");
                taskData.setCreationDate(new java.sql.Timestamp(System.currentTimeMillis()));

                taskController.addTaskCommand(taskData.getChatId(), taskData.getDescription(), taskData.getDeadline(), taskData.getPriority(),
                        taskData.getCreationDate());

                messageSender.sendMessage(chatId, "Task has been added successfully!");
                userState.remove(chatId);
                taskDataMap.remove(chatId);
                break;
        }
    }

    private static class TaskData {
        private long chatId;
        private String description;
        private java.sql.Timestamp deadline;
        private String priority;
        private String status;
        private java.sql.Timestamp creationDate;

        public void setCreationDate(Timestamp creationDate) {
            this.creationDate = creationDate;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public void setChatId(long chatId) {
            this.chatId = chatId;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setDeadline(Timestamp deadline) {
            this.deadline = deadline;
        }

        public String getPriority() {
            return priority;
        }

        public String getStatus() {
            return status;
        }

        public long getChatId() {
            return chatId;
        }

        public String getDescription() {
            return description;
        }

        public Timestamp getCreationDate() {
            return creationDate;
        }

        public Timestamp getDeadline() {
            return deadline;
        }
    }
}
