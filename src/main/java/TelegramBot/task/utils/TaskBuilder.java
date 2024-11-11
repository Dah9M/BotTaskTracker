package TelegramBot.task.utils;

import TelegramBot.task.TaskData;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TaskBuilder implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

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
                return "Task created successfully!";

            default:
                return "Unexpected input.";
        }
    }

    @Override
    public boolean isOperationCompleted(Long chatId) {
        TaskData taskData = taskDataMap.get(chatId);
        return taskData != null && taskData.getStep() >= 3;
    }

    public TaskData getTaskData(long chatId) {
        return taskDataMap.get(chatId);
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
