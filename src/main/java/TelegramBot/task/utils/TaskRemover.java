package TelegramBot.task.utils;

import TelegramBot.task.TaskData;

import java.util.HashMap;
import java.util.Map;

public class TaskRemover implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        return "Please provide the ID of the task you want to delete.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) return "Task deletion not initiated.";

        if (taskData.getStep() == 0) {
            taskData.setDbID(Integer.parseInt(input));
            taskData.nextStep();
            return "Are you sure you want to delete this task? Type 'yes' to confirm or 'no' to cancel.";
        } else if (taskData.getStep() == 1) {
            if ("yes".equalsIgnoreCase(input)) {
                taskData.nextStep();
                return "Task deleted successfully!";
            } else {
                clearOperationData(chatId);
                return "Task deletion canceled.";
            }
        } else {
            return "Unexpected input.";
        }
    }

    @Override
    public boolean isOperationCompleted(Long chatId) {
        TaskData taskData = taskDataMap.get(chatId);
        return taskData != null && taskData.getStep() >= 2;
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }
}