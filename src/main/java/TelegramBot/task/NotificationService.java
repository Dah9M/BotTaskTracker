package TelegramBot.task;

import TelegramBot.model.User;
import TelegramBot.model.UserRepository;
import TelegramBot.service.MessageSender;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService {
    private final TaskService taskService;
    private final MessageSender messageSender;
    private final UserRepository userRepository;

    public NotificationService(TaskService taskService, MessageSender messageSender, UserRepository userRepository) {
        this.taskService = taskService;
        this.messageSender = messageSender;
        this.userRepository = userRepository;

        // Запуск планировщика
        startNotificationScheduler();
    }

    private void startNotificationScheduler() {
        Timer timer = new Timer(true); // Запуск в фоновом режиме
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNotifications(); // Отправка уведомлений
            }
        }, 0, 60 * 1000); // Проверка каждые 60 секунд
    }

    private void sendNotifications() {
        List<User> users = userRepository.getAllUsers();
        Instant now = Instant.now();

        for (User user : users) {
            Long chatId = user.getChatId();
            if (chatId == null) continue;

            List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
            for (TaskData task : tasks) {
                if (task.getDeadline() == null) continue;

                long timeLeft = task.getDeadline().toInstant().toEpochMilli() - now.toEpochMilli();

                if (timeLeft > 0 && timeLeft <= 10 * 60 * 1000) {
                    messageSender.sendMessage(chatId, "⏰ Напоминание! До дедлайна задачи '" + task.getDescription() + "' осталось менее 10 минут!");
                } else if (timeLeft <= 0) {
                    if (task.getDeadlineNotificationCount() < 3) {
                        messageSender.sendMessage(chatId, "❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                        task.setDeadlineNotificationCount(task.getDeadlineNotificationCount() + 1);

                        // Обновляем счетчик в базе данных
                        taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                    }
                }
            }
        }
    }
}
