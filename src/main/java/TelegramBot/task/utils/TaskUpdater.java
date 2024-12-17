package TelegramBot.task.utils;

import TelegramBot.model.TaskCategory;
import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;
import TelegramBot.model.TaskPriority;

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
                if (!taskService.isTaskOwner(chatId, taskData.getDbID())) {
                    taskDataMap.remove(chatId);
                    return "You are not the owner of this task.";
                }
                taskData.nextStep();
                return "Please select the field to update: Description, Deadline, Category or Priority.";

            case 1:
                taskData.setSelectedField(input);
                taskData.nextStep();
                return "Please enter the new value for " + input + ".";

            case 2:
                if ("priority".equalsIgnoreCase(taskData.getSelectedField())) {
                    if (!TaskPriority.isValidPriority(input)) {
                        return "Invalid priority. Please enter one of the following: Low, Medium, High.";
                    }
                    taskData.setNewValue(TaskPriority.valueOf(input.toUpperCase()).name());

                } else if ("category".equalsIgnoreCase(taskData.getSelectedField())) {
                    if (!TaskCategory.isValidCategory(input)) {
                        return "Invalid category. Please enter one of the following: Work, Life, Education.";
                    }
                    taskData.setNewValue(TaskCategory.valueOf(input.toUpperCase()).name());
                } else if ("description".equalsIgnoreCase(taskData.getSelectedField())) {
                    taskData.setNewValue(input);
                } else {
                    return "Invalid field. You can update only Description, Priority, Deadline, or Category.";
                }


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

    @Override
    public TaskData getTaskData(Long chatId) {
        return taskDataMap.get(chatId);
    }
}
