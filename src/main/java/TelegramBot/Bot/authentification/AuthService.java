package TelegramBot.Bot.authentification;

import TelegramBot.model.UserRepository;
import TelegramBot.model.User;
import java.sql.SQLException;

public class AuthService {
    private static AuthService instance;
    private final UserRepository userRepository;

    private AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static synchronized AuthService getInstance(UserRepository userRepository) {
        if (instance == null) {
            instance = new AuthService(userRepository);
        }
        return instance;
    }

    public String registerUser(Long chatId) {
        try {
            if (userRepository.getUserByChatId(chatId) != null) {
                return "Вы уже зарегистрированы.";
            }

            User user = new User(chatId);
            userRepository.registerUser(user);
            return "Вы успешно зарегистрированы!";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка регистрации.";
        }
    }

}
