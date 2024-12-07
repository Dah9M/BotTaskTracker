package TelegramBot.model;

import TelegramBot.auth.AuthController;
import TelegramBot.auth.AuthService;
import TelegramBot.service.Keyboard;
import TelegramBot.service.MessageSender;
import TelegramBot.task.TaskController;
import TelegramBot.task.TaskService;
import lombok.Getter;

@Getter
public class BotUtils {
    private static BotUtils instance;

    @Getter
    private final AuthController authController;
    @Getter
    private final TaskController taskController;
    private final DatabaseConnector databaseConnector;
    private final TaskRepository taskRepository;
    @Getter
    private final UserRepository userRepository;
    @Getter
    private final MessageSender messageSender;
    @Getter
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

