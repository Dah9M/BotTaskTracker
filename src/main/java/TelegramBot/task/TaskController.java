package TelegramBot.task;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TaskController {
    private final TaskService taskService;
    private final TaskBuilder taskBuilder;
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
        this.taskBuilder = new TaskBuilder();
    }

    public String addTaskCommand(Long chatId) {
        return taskBuilder.startTaskCreation(chatId);
    }

    public String handleTaskInput(Long chatId, String input) {
        String message = taskBuilder.processInput(chatId, input);

        if (taskBuilder.isTaskComplete()) {
            TaskData taskData = taskBuilder.getTaskData(chatId);
            taskService.addTask(taskData.getChatId(), taskData.getDescription(), taskData.getDeadline(), taskData.getPriority(), taskData.getCreationDate());
            taskBuilder.clearTaskData(chatId);
            message = "Task has been added successfully!";
        }

        return message;
    }

    public boolean isTaskInProgress(Long chatId) {
        return taskBuilder.isInProgress(chatId);
    }
}
