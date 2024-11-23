package TelegramBot.auth;

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
                return "You've already registered.";
            }

            User user = new User(chatId);
            database.registerUser(user);
            return "Registration successful!";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Registration error.";
        }
    }

    // метод для получения пользователя по chatId
    public User getUserByChatId(Long chatId) throws SQLException {
        return database.getUserByChatId(chatId);
    }
}
