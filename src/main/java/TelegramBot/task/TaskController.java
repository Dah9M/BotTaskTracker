package TelegramBot.task;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Timestamp;

public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public SendMessage addTaskCommand(Long chatId, String description, Timestamp deadline, String priority, Timestamp creation_date) {


        String message = taskService.addTask(chatId, description, deadline, priority, creation_date);

        return new SendMessage(String.valueOf(chatId), message);
    }
}
