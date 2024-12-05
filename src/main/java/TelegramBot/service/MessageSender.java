package TelegramBot.service;

import TelegramBot.task.TaskData;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

// экземпляр бота передаётся только сюда

public class MessageSender {
    private final TelegramLongPollingBot bot;
    @Setter
    @Getter
    private Long currentChatId = null;

    public MessageSender(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(currentChatId));
        sendMessage.setText(message);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendReplyMarkup(ReplyKeyboardMarkup keyboard, String messageText) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(currentChatId));
        sendMessage.setText(messageText);
        sendMessage.setReplyMarkup(keyboard);

        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTasks(List<TaskData> taskList) {
        int number = 1;

        if (taskList.isEmpty()) {
            sendMessage("No tasks found.");
            return;
        }

        StringBuilder messageBuilder = new StringBuilder("Your tasks:\n\n");

        for (TaskData task : taskList) {
            messageBuilder.append(number).append("\n").append("  Description: ").append(task.getDescription()).append("\n")
                    .append("  Deadline: ").append(task.getDeadline()).append("\n")
                    .append("  Priority: ").append(task.getPriority()).append("\n")
                    .append("  Status: ").append(task.getStatus()).append("\n")
                    .append("  Created on: ").append(task.getCreationDate()).append("\n\n");
            number++;
        }

        String message = messageBuilder.toString();

        if (message.length() > 4096) {
            sendLongMessage(message);
        } else {
            sendMessage(message);
        }
    }

    private void sendLongMessage(String message) {
        int maxLength = 4096;
        int start = 0;

        while (start < message.length()) {
            int end = Math.min(start + maxLength, message.length());
            sendMessage(message.substring(start, end));
            start = end;
        }
    }
}

