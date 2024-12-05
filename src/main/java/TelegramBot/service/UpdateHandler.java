package TelegramBot.service;

import TelegramBot.model.BotUtils;
import TelegramBot.model.Commands;
import TelegramBot.task.TaskController;
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
            String response = taskController.handleTaskInput(currentInput);
            messageSender.sendMessage(response);
        } else if (taskController.isUpdateInProgress()) {
            String response = taskController.handleUpdateInput(currentInput);
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
