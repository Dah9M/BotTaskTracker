package TelegramBot.task.utils;

import TelegramBot.task.TaskData;

import java.util.HashMap;
import java.util.Map;

public class TaskUpdater implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        return "Please provide the ID of the task you want to update.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            return "Task update not initiated.";
        }

        switch (taskData.getStep()) {
            case 0:
                taskData.setDbID(Integer.parseInt(input));
                taskData.nextStep();
                return "Please select the field to update: Description, Deadline, or Priority.";

            case 1:
                taskData.setSelectedField(input);
                taskData.nextStep();
                return "Please enter the new value for " + input + ".";

            case 2:
                taskData.setNewValue(input);
                taskData.nextStep();
                return "Updating the task...";
        }
        return "Unexpected error. Please try again.";
    }

    public boolean isOperationCompleted(Long chatId) {
        TaskData taskData = taskDataMap.get(chatId);
        return taskData != null && taskData.getStep() >= 3;
    }

    public TaskData getTaskData(long chatId) {
        return taskDataMap.get(chatId);
    }

    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
