package TelegramBot.task;

import TelegramBot.model.BotUtils;
import TelegramBot.service.NotificationService;
import TelegramBot.task.utils.TaskBuilder;
import TelegramBot.task.utils.TaskOperation;
import TelegramBot.task.utils.TaskRemover;
import TelegramBot.task.utils.TaskUpdater;
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

        // Регистрация операций
        operations.put("create", new TaskBuilder(taskService));
        operations.put("update", new TaskUpdater(taskService));
        operations.put("delete", new TaskRemover(taskService));
    }

    // Инициализация процесса добавления задачи
    public String addTaskCommand() {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс добавления задачи.", chatId);
        return taskBuilder.startOperation(chatId);
    }

    // Инициализация процесса обновления задачи
    public String updateTaskCommand() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс обновления задачи.", chatId);
        return taskUpdater.startOperation(chatId);
    }

    // Инициализация процесса удаления задачи
    public String deleteTaskCommand() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} начал процесс удаления задачи.", chatId);
        return taskRemover.startOperation(chatId);
    }

    // Отображение всех задач с фильтрацией по статусу
    public void viewTasksCommand(String status) {
        chatId = botUtils.getMessageSender().getCurrentChatId();
        List<TaskData> tasks = taskService.getTasksByStatus(chatId, status);
        logger.info("Пользователь {} запросил просмотр задач по статусу: {}", chatId, status);

        if (tasks.isEmpty()) {
            botUtils.getMessageSender().sendMessage("No tasks found.");
            logger.warn("Для пользователя {} не найдено задач по статусу: {}", chatId, status);
        } else {
            botUtils.getMessageSender().sendTasks(tasks);
            logger.info("Отправлено {} задач пользователю {} по статусу: {}", tasks.size(), chatId, status);
        }

        botUtils.getMessageSender().sendReplyMarkup(botUtils.getKeyboard().setMainKeyboard(), "Main menu:");
    }

    public void viewTasksByCategory(String category) {
        chatId = botUtils.getMessageSender().getCurrentChatId();
        List<TaskData> tasks = taskService.getTasksByCategory(chatId, category);
        logger.info("Пользователь {} запросил просмотр задач по категории: {}", chatId, category);

        if (tasks.isEmpty()) {
            botUtils.getMessageSender().sendMessage("No tasks found.");
            logger.warn("Для пользователя {} не найдено задач по категории: {}", chatId, category);
        } else {
            botUtils.getMessageSender().sendTasks(tasks);
            logger.info("Отправлено {} задач пользователю {} по категории: {}", tasks.size(), chatId, category);
        }

        botUtils.getMessageSender().sendReplyMarkup(botUtils.getKeyboard().setMainKeyboard(), "Main menu:");
    }

    public void viewTasksByPriority(String priority) {
        chatId = botUtils.getMessageSender().getCurrentChatId();
        List<TaskData> tasks = taskService.getTasksByPriority(chatId, priority);
        logger.info("Пользователь {} запросил просмотр задач по приоритету: {}", chatId, priority);

        if (tasks.isEmpty()) {
            botUtils.getMessageSender().sendMessage("No tasks found.");
            logger.warn("Для пользователя {} не найдено задач по приоритету: {}", chatId, priority);
        } else {
            botUtils.getMessageSender().sendTasks(tasks);
            logger.info("Отправлено {} задач пользователю {} по приоритету: {}", tasks.size(), chatId, priority);
        }

        botUtils.getMessageSender().sendReplyMarkup(botUtils.getKeyboard().setMainKeyboard(), "Main menu:");
    }

    // Обработка пошагового ввода для добавления задачи
    public String handleTaskInput(String input) {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        logger.info("Пользователь {} вводит данные для добавления задачи: {}", chatId, input);
        return taskBuilder.processInput(chatId, input);
    }

    // Обработка пошагового ввода для обновления задачи
    public String handleUpdateInput(String input) {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        botUtils.getKeyboard().setMainKeyboard();
        logger.info("Пользователь {} вводит данные для обновления задачи: {}", chatId, input);
        return taskUpdater.processInput(chatId, input);
    }

    // Обработка пошагового ввода для удаления задачи
    public String handleDeleteInput(String input) {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        botUtils.getKeyboard().setMainKeyboard();
        logger.info("Пользователь {} вводит данные для удаления задачи: {}", chatId, input);
        return taskRemover.processInput(chatId, input);
    }

    // Проверка, находится ли пользователь в процессе добавления задачи
    public boolean isTaskInProgress() {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskBuilder.isInProgress(chatId);
        logger.debug("Проверка процесса добавления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    // Проверка, находится ли пользователь в процессе обновления задачи
    public boolean isUpdateInProgress() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskUpdater.isInProgress(chatId);
        logger.debug("Проверка процесса обновления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    // Проверка, находится ли пользователь в процессе удаления задачи
    public boolean isDeleteInProgress() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        boolean inProgress = taskRemover.isInProgress(chatId);
        logger.debug("Проверка процесса удаления задачи для пользователя {}: {}", chatId, inProgress);
        return inProgress;
    }

    // Так надо для проверки есть ли таски
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
