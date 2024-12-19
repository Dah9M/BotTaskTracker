package TelegramBot.task.utils;

import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;

import java.util.HashMap;
import java.util.Map;

public class TaskRemover implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskRemover(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        return "Please provide the ID of the task you want to delete.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) return "Task deletion not initiated.";

        if (chatId.toString().matches("^\\d+$")) {
            return "Please enter a valid number.";
        }

        if (taskData.getStep() == 0) {
            taskData.setDbID(Integer.parseInt(input));
            taskData.nextStep();

            if (!taskService.isTaskOwner(chatId, taskData.getDbID())) {
                taskDataMap.remove(chatId);
                return "You are not the owner of this task.";
            }

            return "Are you sure you want to delete this task? Type 'yes' to confirm or 'no' to cancel.";
        } else if (taskData.getStep() == 1) {
            if ("yes".equalsIgnoreCase(input)) {
                String result = taskService.deleteTask(chatId, taskData.getDbID());
                taskDataMap.remove(chatId);
                return result;
            } else {
                clearOperationData(chatId);
                return "Task deletion canceled.";
            }
        } else {
            return "Unexpected input.";
        }
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
    }

    @Override
    public boolean isInProgress(Long chatId) {
        return taskDataMap.containsKey(chatId);
    }

    @Override
    public TaskData getTaskData(Long chatId) {
        return taskDataMap.get(chatId);
    }
}
