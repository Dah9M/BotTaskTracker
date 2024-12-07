package TelegramBot.task;

import TelegramBot.model.BotUtils;
import TelegramBot.model.User;
import TelegramBot.model.UserRepository;
import TelegramBot.service.MessageSender;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService {
    private final TaskService taskService;
    private final MessageSender messageSender;
    private final UserRepository userRepository;

    // Теперь всё в UTC+5
    private static final ZoneId UTC_PLUS_5 = ZoneId.of("UTC+05:00");

    public NotificationService(TaskService taskService, BotUtils botUtils) {
        this.taskService = taskService;
        this.messageSender = botUtils.getMessageSender();
        this.userRepository = botUtils.getUserRepository();
    }

    public void initScheduler() {
        startNotificationScheduler();
    }

    private void startNotificationScheduler() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNotifications();
            }
        }, 0, 60 * 1000);
    }

    private void sendNotifications() {
        if (messageSender == null) {
            System.out.println("messageSender is null. Cannot send notifications.");
            return;
        }

        // Берём текущее время в UTC+5
        ZonedDateTime nowInPlus5 = ZonedDateTime.now(UTC_PLUS_5);
        System.out.println("sendNotifications called at (UTC+5): " + Timestamp.valueOf(nowInPlus5.toLocalDateTime()));

        List<User> users = userRepository.getAllUsers();

        for (User user : users) {
            Long chatId = user.getChatId();
            if (chatId == null) continue;

            List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
            for (TaskData task : tasks) {
                if ("Completed".equalsIgnoreCase(task.getStatus())) {
                    continue;
                }

                Timestamp deadlineLocal = task.getDeadline();
                if (deadlineLocal == null) continue;

                // Дедлайн из БД интерпретируем как локальное время UTC+5
                LocalDateTime deadlineLDT = deadlineLocal.toLocalDateTime();
                ZonedDateTime deadlineInPlus5 = deadlineLDT.atZone(UTC_PLUS_5);

                long timeLeft = deadlineInPlus5.toInstant().toEpochMilli() - nowInPlus5.toInstant().toEpochMilli();

                String priority = (task.getPriority() == null) ? "low" : task.getPriority().toLowerCase();
                int count = task.getDeadlineNotificationCount();

                Timestamp lastNotifyTime = task.getLastNotificationTime();
                long sinceLastNotify = (lastNotifyTime == null) ? Long.MAX_VALUE :
                        nowInPlus5.toInstant().toEpochMilli() - lastNotifyTime.toInstant().toEpochMilli();

                // Расчёт времени между созданием и дедлайном в UTC+5
                long totalDuration = deadlineInPlus5.toInstant().toEpochMilli() - task.getCreationDate().toInstant().toEpochMilli();
                boolean originallyMoreThanDay = totalDuration > 24 * 60 * 60 * 1000;

                System.out.println("-----------------------------------------");
                System.out.println("Checking task: " + task.getDescription());
                System.out.println("Priority: " + priority + ", timeLeft(ms): " + timeLeft + ", count: " + count + ", originallyMoreThanDay: " + originallyMoreThanDay);
                System.out.println("Now(UTC+5): " + Timestamp.valueOf(nowInPlus5.toLocalDateTime()) + ", Deadline(UTC+5): " + Timestamp.valueOf(deadlineInPlus5.toLocalDateTime()) + ", sinceLastNotify(ms): " + sinceLastNotify);
                System.out.println("CreationDate(UTC+5 интерпретация): " + task.getCreationDate());
                System.out.println("-----------------------------------------");

                messageSender.setCurrentChatId(chatId);

                boolean messageSent = false;

                switch (priority) {
                    case "low":
                        if (timeLeft <= 0 && count < 1) {
                            messageSent = safeSendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                            if (messageSent) {
                                task.setDeadlineNotificationCount(count + 1);
                                taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                            }
                        }
                        break;

                    case "medium":
                        if (originallyMoreThanDay && timeLeft > 0 && timeLeft <= 24 * 60 * 60 * 1000 && count == 0) {
                            messageSent = safeSendMessage("⏳ До дедлайна задачи '" + task.getDescription() + "' остался 1 день!");
                            if (messageSent) {
                                task.setDeadlineNotificationCount(1);
                                taskService.updateTaskNotificationCount(task.getDbID(), 1);
                            }
                        } else if (timeLeft > 0 && timeLeft <= 60 * 60 * 1000 && count == (originallyMoreThanDay ? 1 : 0)) {
                            messageSent = safeSendMessage("⏳ До дедлайна задачи '" + task.getDescription() + "' остался 1 час!");
                            if (messageSent) {
                                task.setDeadlineNotificationCount(count + 1);
                                taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                            }
                        } else if (timeLeft <= 0 && count < 3) {
                            messageSent = safeSendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                            if (messageSent) {
                                task.setDeadlineNotificationCount(3);
                                taskService.updateTaskNotificationCount(task.getDbID(), 3);
                            }
                        }
                        break;

                    case "high":
                        long fourHours = 4 * 60 * 60 * 1000;
                        long oneHour = 60 * 60 * 1000;
                        if (timeLeft > 0) {
                            if (sinceLastNotify >= fourHours) {
                                long hoursLeft = timeLeft / (60 * 60 * 1000);
                                messageSent = safeSendMessage("🔔 Задача '" + task.getDescription() + "' истечет через примерно " + hoursLeft + " ч.");
                                if (messageSent) {
                                    Timestamp nowTs = Timestamp.valueOf(nowInPlus5.toLocalDateTime());
                                    task.setLastNotificationTime(nowTs);
                                    taskService.updateTaskLastNotifyTime(task.getDbID(), nowTs);
                                }
                            }
                        } else {
                            if (count < 3 && sinceLastNotify >= oneHour) {
                                messageSent = safeSendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                                if (messageSent) {
                                    task.setDeadlineNotificationCount(count + 1);
                                    Timestamp nowTs = Timestamp.valueOf(nowInPlus5.toLocalDateTime());
                                    task.setLastNotificationTime(nowTs);
                                    taskService.updateTaskNotificationCount(task.getDbID(), count + 1);
                                    taskService.updateTaskLastNotifyTime(task.getDbID(), nowTs);
                                }
                            }
                        }
                        break;

                    default:
                        if (timeLeft <= 0 && count < 1) {
                            messageSent = safeSendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                            if (messageSent) {
                                task.setDeadlineNotificationCount(count + 1);
                                taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                            }
                        }
                        break;
                }
            }
        }
    }

    private boolean safeSendMessage(String text) {
        try {
            messageSender.sendMessage(text);
            System.out.println("Message sent: " + text);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

