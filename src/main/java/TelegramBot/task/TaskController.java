package TelegramBot.task;

import TelegramBot.service.MessageSender;
import TelegramBot.task.utils.TaskBuilder;
import TelegramBot.task.utils.TaskOperation;
import TelegramBot.task.utils.TaskRemover;
import TelegramBot.task.utils.TaskUpdater;
import TelegramBot.model.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskController {
    private final NotificationService notificationService;
    private final Map<String, TaskOperation> operations = new HashMap<>();
    private final MessageSender messageSender;
    private final TaskService taskService;

    public TaskController(TaskService taskService, MessageSender messageSender, UserRepository userRepository) {
        this.messageSender = messageSender;
        this.taskService = taskService;
        this.notificationService = new NotificationService(taskService, messageSender, userRepository);

        // Регистрация операций
        operations.put("create", new TaskBuilder(taskService));
        operations.put("update", new TaskUpdater(taskService));
        operations.put("delete", new TaskRemover(taskService));
    }

    // Инициализация процесса добавления задачи
    public String addTaskCommand(Long chatId) {
        TaskOperation taskBuilder = operations.get("create");
        return taskBuilder.startOperation(chatId);
    }

    // Инициализация процесса обновления задачи
    public String updateTaskCommand(Long chatId) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.startOperation(chatId);
    }

    // Отображение всех задач с фильтрацией по статусу
    public void viewTasksCommand(Long chatId, String status) {
        String tasks = taskService.getTasksByStatus(chatId, status);
        messageSender.sendMessage(chatId, tasks);
    }

    // Обработка пошагового ввода для добавления задачи
    public String handleTaskInput(Long chatId, String input) {
        TaskOperation taskBuilder = operations.get("create");
        return taskBuilder.processInput(chatId, input);
    }

    // Обработка пошагового ввода для обновления задачи
    public String handleUpdateInput(Long chatId, String input) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.processInput(chatId, input);
    }

    // Проверка, находится ли пользователь в процессе добавления задачи
    public boolean isTaskInProgress(Long chatId) {
        TaskOperation taskBuilder = operations.get("create");
        return ((TaskBuilder) taskBuilder).isInProgress(chatId);
    }

    // Проверка, находится ли пользователь в процессе обновления задачи
    public boolean isUpdateInProgress(Long chatId) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.isOperationCompleted(chatId);
    }

    // для удаление таски
    public String deleteTaskCommand(Long chatId) {
        TaskOperation taskRemover = operations.get("delete");
        return taskRemover.startOperation(chatId);
    }

    // так надо для проверки есть ли таски
    public boolean hasTasks(Long chatId) {
        List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
        return !tasks.isEmpty();
    }
}
