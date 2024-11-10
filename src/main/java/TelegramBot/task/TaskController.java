package TelegramBot.task;

import TelegramBot.service.MessageSender;

import java.util.HashMap;
import java.util.Map;

public class TaskController {
    private final TaskService taskService;
    private final TaskBuilder taskBuilder;
    private final TaskUpdater taskUpdater;
    private final MessageSender messageSender;
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

    public TaskController(TaskService taskService, MessageSender messageSender) {
        this.taskService = taskService;
        this.messageSender = messageSender;
        this.taskBuilder = new TaskBuilder();
        this.taskUpdater = new TaskUpdater();
    }

    public String addTaskCommand(Long chatId) {
        return taskBuilder.startTaskCreation(chatId);
    }

    public void viewTasksCommand(Long chatId, String key) {
        messageSender.sendTasks(chatId, taskService.viewTasks(chatId, key));
    }

    public String updateTaskCommand(Long chatId) {
        return taskUpdater.startTaskUpdate(chatId);
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

    public String handleUpdateInput(Long chatId, String input) {
        String response = taskUpdater.processUpdateInput(chatId, input);

        if (taskUpdater.isUpdateComplete()) {
            TaskData updatedTaskData = taskUpdater.getTaskData(chatId);
            taskService.updateTaskField(updatedTaskData.getChatId(), updatedTaskData.getSelectedField(), updatedTaskData.getNewValue());
            taskUpdater.clearTaskData(chatId);
            response = "Task updated successfully!";
        }
        return response;
    }

    public boolean isTaskInProgress(Long chatId) {
        return taskBuilder.isInProgress(chatId);
    }

    public boolean isUpdateInProgress(Long chatId) {
        return taskUpdater.isInProgress(chatId);
    }
}
