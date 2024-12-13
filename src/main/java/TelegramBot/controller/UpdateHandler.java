package TelegramBot.controller;

import TelegramBot.model.BotUtils;
import TelegramBot.model.Commands;
import TelegramBot.model.TaskCategory;
import TelegramBot.service.MessageSender;
import TelegramBot.task.TaskController;
import TelegramBot.model.TaskPriority;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandler {
    private final MessageSender messageSender;
    private final BotUtils botUtils;
    private final Commands commands;

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);
        this.botUtils = BotUtils.getInstance(messageSender);
        this.commands = Commands.getInstance(botUtils);
    }

    public void handleTextMessage(Update update) {
        Long currentChatId = update.getMessage().getChatId();
        String currentInput = update.getMessage().getText();
        TaskController taskController = botUtils.getTaskController();

        messageSender.setCurrentChatId(currentChatId);

        if (taskController.isTaskInProgress()) {
            if (taskController.isWaitingForPriorityInput()) {
                if (!TaskPriority.isValidPriority(currentInput)) {
                    messageSender.sendMessage("Invalid priority. Please enter Low, Medium, or High.");
                } else {
                    messageSender.sendMessage(taskController.handleTaskInput(currentInput));
                }
            } else {
                messageSender.sendMessage(taskController.handleTaskInput(currentInput));
            }
        } else if (taskController.isUpdateInProgress()) {
            if (taskController.isWaitingForCategoryInput()) { // Добавляем метод
                if (!TaskCategory.isValidCategory(currentInput)) {
                    messageSender.sendMessage("Invalid category. Please enter Work, Life, or Education.");
                } else {
                    String response = taskController.handleUpdateInput(currentInput);
                    messageSender.sendMessage(response);
                }
            } else {
                String response = taskController.handleUpdateInput(currentInput);
                messageSender.sendMessage(response);
            }

        } else if (taskController.isDeleteInProgress()) {
            String response = taskController.handleDeleteInput(currentInput);
            messageSender.sendMessage(response);
        } else {
            Runnable command = commands.getCommand(currentInput);
            if (command != null) {
                command.run();
            } else {
                messageSender.sendMessage("Unknown command.");
            }
        }
    }
}
