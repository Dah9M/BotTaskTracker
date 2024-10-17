package TelegramBot.Bot.authentification;

import TelegramBot.model.UserRepository;
import TelegramBot.model.User;

import java.sql.SQLException;

// Класс должен реализовывать основную логику авторизации, регистрации, добавление в базу данных
// но все команды должен дёргать из вспомогательных классов

public class AuthService {
    private final UserRepository database;


    public AuthService(UserRepository database) {
        this.database = database;
    }

    public String registerUser(Long chatId, String username, String password) {

        if (!isValidUsername(username) || !isValidPassword(password)) {
            return "Невалидные символы для имени или пароля";
        }

        try {
            if (database.getUserByChatId(chatId) != null) {
                return "Пользователь уже зарегистрирован";
            }

            User user = new User(chatId, username, password);
            database.registerUser(user);
            return "Пользователь успешно зарегистрирован!";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка регистрации";
        }
    }

    public String authorizeUser(Long chatId, String password) {
        try {
            if (database.authentificateUser(chatId, password)) {
                return "Вы успешно авторизованы";
            } else {
                return "Неверное пароль";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка авторизации. Попробуйте ещё раз";
        }
    }

    public boolean isValidUsername(String username) {
        return username.matches("^[a-zA-Z0-9]*$");
    }

    public boolean isValidPassword(String password) {
        return password.matches("^[a-zA-Z0-9]*$");
    }
}
