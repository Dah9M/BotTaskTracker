package telegrambot.task;

import telegrambot.model.BotUtils;
import telegrambot.service.NotificationService;
import telegrambot.task.utils.TaskBuilder;
import telegrambot.task.utils.TaskOperation;
import telegrambot.task.utils.TaskRemover;
import telegrambot.task.utils.TaskUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final Map<String, TaskOperation> operations = new HashMap<>();
    private final TaskService taskService;
    private final NotificationService notificationService;
    private Long chatId = null;
    private final BotUtils botUtils;

    public TaskController(TaskService taskService, BotUtils botUtils) {
        this.botUtils = botUtils;
        this.taskService = taskService;
        this.notificationService = new NotificationService(taskService, botUtils);

        operations.put("create", new TaskBuilder(taskService));
        operations.put("update", new TaskUpdater(taskService));
        operations.put("delete", new TaskRemover(taskService));
    }

    public String addTaskCommand() {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс добавления задачи.", chatId);
        return taskBuilder.startOperation(chatId);
    }

    public String updateTaskCommand() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс обновления задачи.", chatId);
        return taskUpdater.startOperation(chatId);
    }

    public String deleteTaskCommand() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс удаления задачи.", chatId);
        return taskRemover.startOperation(chatId);
    }

    public void viewTasksCommand(String key) {
        chatId = botUtils.getMessageSender().getCurrentChatId();
        List<TaskData> tasks = taskService.getTasksByKey(chatId, key);
        logger.info("Пользователь {} запросил просмотр задач по ключуу: {}", chatId, key);

        if (tasks.isEmpty()) {
            botUtils.getMessageSender().sendMessage("No tasks found.");
            logger.warn("Для пользователя {} не найдено задач по ключу: {}", chatId, key);
        } else {
            botUtils.getMessageSender().sendTasks(tasks);
            logger.info("Отправлено {} задач пользователю {} по ключу: {}", tasks.size(), chatId, key);
        }

        botUtils.getMessageSender().sendReplyMarkup(botUtils.getKeyboard().setMainKeyboard(), "Main menu:");
    }

    public String handleTaskInput(String input) {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} вводит данные для добавления задачи: {}", chatId, input);
        return taskBuilder.processInput(chatId, input);
    }

    public String handleUpdateInput(String input) {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        botUtils.getKeyboard().setMainKeyboard();
        logger.info("Пользователь {} вводит данные для обновления задачи: {}", chatId, input);
        return taskUpdater.processInput(chatId, input);
    }

    public String handleDeleteInput(String input) {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        botUtils.getKeyboard().setMainKeyboard();
        logger.info("Пользователь {} вводит данные для удаления задачи: {}", chatId, input);
        return taskRemover.processInput(chatId, input);
    }

    public boolean isTaskInProgress() {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskBuilder.isInProgress(chatId);
        logger.debug("Проверка процесса добавления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    public boolean isUpdateInProgress() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskUpdater.isInProgress(chatId);
        logger.debug("Проверка процесса обновления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    public boolean isDeleteInProgress() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskRemover.isInProgress(chatId);
        logger.debug("Проверка процесса удаления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    public boolean hasTasks() {
        List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
        boolean hasTasks = !tasks.isEmpty();
        logger.debug("Проверка наличия задач для пользователя {}: {}", chatId, hasTasks);
        return hasTasks;
    }

    public boolean isWaitingForPriorityInput() {
        TaskOperation taskBuilder = operations.get("create");
        if (taskBuilder instanceof TaskBuilder) {
            Long chatId = botUtils.getMessageSender().getCurrentChatId();
            TaskBuilder builder = (TaskBuilder) taskBuilder;
            TaskData taskData = builder.getTaskData(chatId);
            boolean waiting = taskData != null && taskData.getStep() == 2; // Шаг 2 — ввод приоритета
            logger.debug("Проверка ожидания ввода приоритета для пользователя {}: {}", chatId, waiting);
            return waiting;
        }
        return false;
    }

    public boolean isWaitingForCategoryInput() {
        TaskOperation taskUpdater = operations.get("update");
        if (taskUpdater instanceof TaskUpdater) {
            TaskUpdater updater = (TaskUpdater) taskUpdater;
            TaskData taskData = updater.getTaskData(chatId);
            boolean waiting = taskData != null && "category".equalsIgnoreCase(taskData.getSelectedField());
            logger.debug("Проверка ожидания ввода категории для пользователя {}: {}", chatId, waiting);
            return waiting;
        }
        return false;
    }

}
