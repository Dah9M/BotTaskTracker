package telegrambot.service;

import lombok.extern.slf4j.Slf4j;
import telegrambot.task.TaskData;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.StringSubstitutor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
            log.error("Ошибка при попытке отправить сообщение: {}", message, e);
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
            log.error("Ошибка при попытке отправить клавиатуру: {}", messageText, e);
        }
    }

    public void sendTasks(List<TaskData> taskList) {
        if (taskList.isEmpty()) {
            sendMessage("No tasks found.");
            return;
        }

        int number = 1;
        StringBuilder messageBuilder = new StringBuilder("Your tasks:\n\n");

        for (TaskData task : taskList) {
            Map<String, String> valuesMap = new HashMap<>();
            valuesMap.put("number", String.valueOf(number));
            valuesMap.put("id", String.valueOf(task.getDbID()));
            valuesMap.put("description", task.getDescription() != null ? task.getDescription() : "N/A");
            valuesMap.put("deadline", task.getDeadline() != null ? String.valueOf(task.getDeadline()) : "N/A");
            valuesMap.put("priority", task.getPriority() != null ? String.valueOf(task.getPriority()) : "N/A");
            valuesMap.put("status", task.getStatus() != null ? String.valueOf(task.getStatus()) : "N/A");
            valuesMap.put("creationDate", task.getCreationDate() != null ? String.valueOf(task.getCreationDate()) : "N/A");
            valuesMap.put("category", task.getCategory() != null ? String.valueOf(task.getCategory()) : "Not Specified");

            // Шаблон для вывода задачи
            String template = """
                    ${number}.
                      📌 Task ID: ${id}
                      📝 Description: ${description}
                      ⏳ Deadline: ${deadline}
                      🔥 Priority: ${priority}
                      ✅ Status: ${status}
                      📅 Created on: ${creationDate}
                      🗂 Category: ${category}
                    """;

            StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
            messageBuilder.append(substitutor.replace(template)).append("\n");

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
