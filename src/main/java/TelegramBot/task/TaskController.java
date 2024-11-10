package TelegramBot.task;

import TelegramBot.service.MessageSender;

import java.util.HashMap;
import java.util.Map;

public class TaskController {
    private final TaskService taskService;
    private final TaskBuilder taskBuilder;
    private final MessageSender messageSender;
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

    public TaskController(TaskService taskService, MessageSender messageSender) {
        this.taskService = taskService;
        this.taskBuilder = new TaskBuilder();
        this.messageSender = messageSender;
    }

    public String addTaskCommand(Long chatId) {
        return taskBuilder.startTaskCreation(chatId);
    }

    public void viewTasksCommand(Long chatId, String key) {
        messageSender.sendTasks(chatId, taskService.viewTasks(chatId, key));
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
