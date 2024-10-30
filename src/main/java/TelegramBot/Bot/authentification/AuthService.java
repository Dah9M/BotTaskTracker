package TelegramBot.Bot.authentification;

import TelegramBot.model.UserRepository;
import TelegramBot.model.User;
import java.sql.SQLException;

public class AuthService {
    private final UserRepository database;

    public AuthService(UserRepository database) {
        this.database = database;
    }

    public String registerUser(Long chatId) {
        try {
            if (database.getUserByChatId(chatId) != null) {
                return "Вы уже зарегистрированы.";
            }

            User user = new User(chatId);
            database.registerUser(user);
            return "Вы успешно зарегистрированы!";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка регистрации.";
        }
    }
}
