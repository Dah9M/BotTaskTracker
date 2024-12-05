package TelegramBot.task.utils;

import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TaskBuilder implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskBuilder(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        return "Please provide a description for the task.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            return "Task creation process has not started. Please initiate by clicking 'Add Task'.";
        }

        switch (taskData.getStep()) {
            case 0:
                taskData.setDescription(input);
                taskData.nextStep();
                return "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS).";

            case 1:
                try {
                    taskData.setDeadline(Timestamp.valueOf(input));
                    taskData.nextStep();
                    return "Please provide a priority for the task (e.g., High, Medium, Low).";
                } catch (IllegalArgumentException e) {
                    return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                }

            case 2:
                taskData.setPriority(input);
                taskData.setCreationDate(new Timestamp(System.currentTimeMillis()));
                String result = taskService.addTask(
                        taskData.getChatId(),
                        taskData.getDescription(),
                        taskData.getDeadline(),
                        taskData.getPriority(),
                        taskData.getCreationDate()
                );
                taskDataMap.remove(chatId);
                return result;

            default:
                return "Unexpected input.";
        }
    }

    @Override
    public boolean isOperationCompleted(Long chatId) {
        TaskData taskData = taskDataMap.get(chatId);
        return taskData != null && taskData.getStep() >= 3;
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }

    @Override
    public boolean isInProgress(Long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
