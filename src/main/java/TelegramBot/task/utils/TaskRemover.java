package TelegramBot.task.utils;

import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class TaskRemover implements TaskOperation {
    private static final Logger logger = LoggerFactory.getLogger(TaskRemover.class);
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskService taskService;

    public TaskRemover(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String startOperation(Long chatId) {
        taskDataMap.put(chatId, new TaskData(chatId));
        logger.info("Пользователь {} начал процесс удаления задачи.", chatId);
        return "Please provide the ID of the task you want to delete.";
    }

    @Override
    public String processInput(Long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            logger.warn("Пользователь {} попытался удалить задачу без инициализации процесса.", chatId);
            return "Task deletion not initiated.";
        }

        if (!chatId.toString().matches("^\\d+$")) {
            logger.warn("Пользователь {} ввёл некорректный ID задачи для удаления: {}", chatId, input);
            return "Please enter a valid number.";
        }

        if (taskData.getStep() == 0) {
            try {
                int dbID = Integer.parseInt(input);
                taskData.setDbID(dbID);
                taskData.nextStep();

                if (!taskService.isTaskOwner(chatId, dbID)) {
                    taskDataMap.remove(chatId);
                    logger.warn("Пользователь {} попытался удалить задачу с ID {}, которой он не владеет.", chatId, dbID);
                    return "You are not the owner of this task.";
                }

                logger.debug("Пользователь {} выбрал задачу с ID {} для удаления.", chatId, dbID);
                return "Are you sure you want to delete this task? Type 'yes' to confirm or 'no' to cancel.";
            } catch (NumberFormatException e) {
                logger.error("Пользователь {} ввёл некорректный формат ID задачи: {}", chatId, input, e);
                return "Invalid task ID format. Please enter a numeric ID.";
            }
        } else if (taskData.getStep() == 1) {
            if ("yes".equalsIgnoreCase(input)) {
                String result = taskService.deleteTask(chatId, taskData.getDbID());
                taskDataMap.remove(chatId);
                logger.info("Пользователь {} подтвердил удаление задачи с ID {}: {}", chatId, taskData.getDbID(), result);
                return result;
            } else {
                taskDataMap.remove(chatId);
                logger.info("Пользователь {} отменил удаление задачи.", chatId);
                return "Task deletion canceled.";
            }
        } else {
            logger.warn("Пользователь {} ввёл неожиданный шаг процесса удаления задачи: {}", chatId, taskData.getStep());
            return "Unexpected input.";
        }
    }

    @Override
    public void clearOperationData(Long chatId) {
        taskDataMap.remove(chatId);
        logger.info("Данные процесса удаления задачи для пользователя {} очищены.", chatId);
    }

    @Override
    public boolean isInProgress(Long chatId) {
        boolean inProgress = taskDataMap.containsKey(chatId);
        logger.debug("Проверка процесса удаления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    @Override
    public TaskData getTaskData(Long chatId) {
        return taskDataMap.get(chatId);
    }
}
