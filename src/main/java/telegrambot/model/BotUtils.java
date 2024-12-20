package telegrambot.model;

import telegrambot.auth.AuthController;
import telegrambot.auth.AuthService;
import telegrambot.service.Keyboard;
import telegrambot.service.MessageSender;
import telegrambot.task.TaskController;
import telegrambot.task.TaskService;
import lombok.Getter;

import javax.inject.Singleton;

@Getter
public class BotUtils {
    private static BotUtils instance;

    private final AuthController authController;
    private final TaskController taskController;
    private final DatabaseConnector databaseConnector;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final MessageSender messageSender;
    private final Keyboard keyboard;


    private BotUtils(MessageSender messageSender) {
        this.messageSender = messageSender;
        this.databaseConnector = new DatabaseConnector();
        this.userRepository = new UserRepository(databaseConnector);
        this.taskRepository = new TaskRepository(databaseConnector);
        this.authController = new AuthController(new AuthService(userRepository));
        this.taskController = new TaskController(new TaskService(taskRepository), this);
        this.keyboard = new Keyboard();
    }

    public static BotUtils getInstance(MessageSender messageSender) {
        if (instance == null) {
            synchronized (BotUtils.class) {
                if (instance == null) {
                    instance = new BotUtils(messageSender);
                }
            }

        }
        return instance;
    }
}
