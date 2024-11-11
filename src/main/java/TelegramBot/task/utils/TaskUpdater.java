package TelegramBot.task.utils;

import TelegramBot.task.TaskData;

import java.util.HashMap;
import java.util.Map;

public class TaskUpdater {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private boolean isUpdateCompleted = false;

    public String startTaskUpdate(long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        isUpdateCompleted = false;
        return "Please provide the ID of the task you want to update.";
    }

    public String processUpdateInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            return "Update process has not started. Please initiate by selecting 'Update Task'.";
        }

        switch (taskData.getStep()) {
            case 0:
                try {
                    int taskId = Integer.parseInt(input);
                    taskData.setDbID(taskId);
                    taskData.nextStep();
                    return "Please select the field to update: Description, Deadline, or Priority.";
                } catch (NumberFormatException e) {
                    return "Invalid ID format. Please enter a numeric ID.";
                }

            case 1:
                String field = input.toLowerCase();
                if (field.equals("description") || field.equals("deadline") || field.equals("priority")) {
                    taskData.setSelectedField(field);
                    taskData.nextStep();
                    return "Please enter the new value for " + field + ".";
                } else {
                    return "Invalid field. Choose Description, Deadline, or Priority.";
                }

            case 2:
                taskData.setNewValue(input);
                isUpdateCompleted = true;
                return "Updating the task...";
        }
        return "Unexpected error. Please try again.";
    }

    public boolean isUpdateComplete() {
        return isUpdateCompleted;
    }

    public TaskData getTaskData(long chatId) {
        return taskDataMap.get(chatId);
    }

    public void clearTaskData(long chatId) {
        taskDataMap.remove(chatId);
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
