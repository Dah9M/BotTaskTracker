package TelegramBot.service;

import TelegramBot.model.Task;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class MessageSender {
    private final TelegramLongPollingBot bot;

    public MessageSender(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(message);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendReplyMarkup(Long chatId, InlineKeyboardMarkup keyboard, String messageText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboard);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTasks(Long chatId, List<Task> taskList) {
        if (taskList.isEmpty()) {
            sendMessage(chatId, "No tasks found.");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder("Your tasks:\n\n");

        for (Task task : taskList) {
            messageBuilder.append("• Description: ").append(task.getDescription()).append("\n")
                    .append("  Deadline: ").append(task.getDeadline()).append("\n")
                    .append("  Priority: ").append(task.getPriority()).append("\n")
                    .append("  Status: ").append(task.getStatus()).append("\n")
                    .append("  Created on: ").append(task.getCreationDate()).append("\n\n");
        }

        String message = messageBuilder.toString();

        if (message.length() > 4096) {
            sendLongMessage(chatId, message);
        } else {
            sendMessage(chatId, message);
        }
    }

    private void sendLongMessage(Long chatId, String message) {
        int maxLength = 4096;
        int start = 0;

        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            sendMessage(chatId, message.substring(start, end));
            start = end;
        }
    }
}

