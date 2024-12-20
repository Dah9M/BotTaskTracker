package telegrambot.task.utils;

import lombok.NonNull;
import telegrambot.task.TaskData;
import telegrambot.task.TaskService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TaskRemover implements TaskOperation {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskRemover(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(@NonNull Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        log.info("Пользователь {} начал процесс удаления задачи.", chatId);
        return "Please provide the ID of the task you want to delete.";
    }

    @Override
    public String processInput(@NonNull Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            log.warn("Пользователь {} попытался удалить задачу без инициализации процесса.", chatId);
            return "Task deletion not initiated.";
        }

        if (!chatId.toString().matches("^\\d+$")) {
            log.warn("Пользователь {} ввёл некорректный ID задачи для удаления: {}", chatId, input);
            return "Please enter a valid number.";
        }

        if (taskData.getStep() == 0) {
            try {
                int dbID = Integer.parseInt(input);
                taskData.setDbID(dbID);
                taskData.nextStep();

                if (!taskService.isTaskOwner(chatId, dbID)) {
                    taskDataMap.remove(chatId);
                    log.warn("Пользователь {} попытался удалить задачу с ID {}, которой он не владеет.", chatId, dbID);
                    return "You are not the owner of this task.";
                }

                log.debug("Пользователь {} выбрал задачу с ID {} для удаления.", chatId, dbID);
                return "Are you sure you want to delete this task? Type 'yes' to confirm or 'no' to cancel.";
            } catch (NumberFormatException e) {
                log.error("Пользователь {} ввёл некорректный формат ID задачи: {}", chatId, input, e);
                return "Invalid task ID format. Please enter a numeric ID.";
            }
        } else if (taskData.getStep() == 1) {
            if ("yes".equalsIgnoreCase(input)) {
                String result = taskService.deleteTask(chatId, taskData.getDbID());
                taskDataMap.remove(chatId);
                log.info("Пользователь {} подтвердил удаление задачи с ID {}: {}", chatId, taskData.getDbID(), result);
                return result;
            } else {
                taskDataMap.remove(chatId);
                log.info("Пользователь {} отменил удаление задачи.", chatId);
                return "Task deletion canceled.";
            }
        } else {
            log.warn("Пользователь {} ввёл неожиданный шаг процесса удаления задачи: {}", chatId, taskData.getStep());
            return "Unexpected input.";
        }
    }

    @Override
    public void clearOperationData(@NonNull Long chatId) {
        taskDataMap.remove(chatId);
        log.info("Данные процесса удаления задачи для пользователя {} очищены.", chatId);
    }

    @Override
    public boolean isInProgress(@NonNull Long chatId) {
        boolean inProgress = taskDataMap.containsKey(chatId);
        log.debug("Проверка процесса удаления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    @Override
    public TaskData getTaskData(@NonNull Long chatId) {
        return taskDataMap.get(chatId);
    }
}
