package TelegramBot.service;

import TelegramBot.model.BotUtils;
import TelegramBot.model.User;
import TelegramBot.model.UserRepository;
import TelegramBot.task.TaskData;
import TelegramBot.task.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final TaskService taskService;
    private final MessageSender messageSender;
    private final UserRepository userRepository;

    public NotificationService(TaskService taskService, BotUtils botUtils) {
        this.taskService = taskService;
        this.messageSender = botUtils.getMessageSender();
        this.userRepository = botUtils.getUserRepository();

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
        logger.info("Планировщик уведомлений запущен.");
    }

    private void sendNotifications() {
        List<User> users = userRepository.getAllUsers();
        Instant now = Instant.now();

        for (User user : users) {
            Long chatId = user.getChatId();
            if (chatId == null) continue;

            try {
                List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
                for (TaskData task : tasks) {
                    if (task.getDeadline() == null) continue;

                    long timeLeft = task.getDeadline().toInstant().toEpochMilli() - now.toEpochMilli();

                    if (timeLeft > 0 && timeLeft <= 10 * 60 * 1000) {
                        // происходит изменение chat_id, может изменится поведение (сомнительное изменение)
                        messageSender.setCurrentChatId(task.getChatId());
                        messageSender.sendMessage("⏰ Напоминание! До дедлайна задачи '" + task.getDescription() + "' осталось менее 10 минут!");
                        logger.info("Отправлено напоминание о задаче {} пользователю {}.", task.getDescription(), chatId);
                    } else if (timeLeft <= 0) {
                        if (task.getDeadlineNotificationCount() < 3) {
                            messageSender.setCurrentChatId(task.getChatId());
                            messageSender.sendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                            task.setDeadlineNotificationCount(task.getDeadlineNotificationCount() + 1);

                            // Обновляем счетчик в базе данных
                            boolean updated = taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                            if (updated) {
                                logger.info("Обновлён счетчик уведомлений для задачи {} пользователя {}.", task.getDbID(), chatId);
                            } else {
                                logger.warn("Не удалось обновить счетчик уведомлений для задачи {} пользователя {}.", task.getDbID(), chatId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при отправке уведомлений для chatId: {}", chatId, e);
            }
        }
    }
}
