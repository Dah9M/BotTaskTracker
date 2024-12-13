package TelegramBot.task;

import TelegramBot.model.BotUtils;
import TelegramBot.task.utils.TaskBuilder;
import TelegramBot.task.utils.TaskOperation;
import TelegramBot.task.utils.TaskRemover;
import TelegramBot.task.utils.TaskUpdater;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TaskController {
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
        return taskBuilder.startOperation(chatId);
    }

    // Инициализация процесса обновления задачи
    public String updateTaskCommand() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskUpdater.startOperation(chatId);
    }

    // Инициализация процесса удаления задачи
    public String deleteTaskCommand() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskRemover.startOperation(chatId);
    }

    // Отображение всех задач с фильтрацией по статусу
    public void viewTasksCommand(String status) {
        chatId = botUtils.getMessageSender().getCurrentChatId();
        String tasks = taskService.getTasksByStatus(chatId, status);
        botUtils.getMessageSender().sendMessage(tasks);
    }

    // Обработка пошагового ввода для добавления задачи
    public String handleTaskInput(String input) {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskBuilder.processInput(chatId, input);
    }

    // Обработка пошагового ввода для обновления задачи
    public String handleUpdateInput(String input) {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskUpdater.processInput(chatId, input);
    }

    // Обработка пошагового ввода для удаления задачи
    public String handleDeleteInput(String input) {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskRemover.processInput(chatId, input);
    }

    // Проверка, находится ли пользователь в процессе добавления задачи
    public boolean isTaskInProgress() {
        TaskOperation taskBuilder = operations.get("create");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskBuilder.isInProgress(chatId);
    }

    // Проверка, находится ли пользователь в процессе обновления задачи
    public boolean isUpdateInProgress() {
        TaskOperation taskUpdater = operations.get("update");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskUpdater.isInProgress(chatId);
    }

    // Проверка, находится ли пользователь в процессе удаления задачи
    public boolean isDeleteInProgress() {
        TaskOperation taskRemover = operations.get("delete");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return taskRemover.isInProgress(chatId);
    }

    // так надо для проверки есть ли таски
    public boolean hasTasks() {
        List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
        chatId = botUtils.getMessageSender().getCurrentChatId();
        return !tasks.isEmpty();
    }
}
