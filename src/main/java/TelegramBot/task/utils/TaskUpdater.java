package TelegramBot.task.utils;

import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;

import java.util.HashMap;
import java.util.Map;

public class TaskUpdater implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskUpdater(TaskService taskService) {
        this.taskService = taskService;
    }

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

                // обновляем задачу в бд
                String result = taskService.updateTaskField(
                        chatId,
                        taskData.getDbID(),
                        taskData.getSelectedField(),
                        taskData.getNewValue()
                );

                taskDataMap.remove(chatId);
                return result;

            default:
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
}
