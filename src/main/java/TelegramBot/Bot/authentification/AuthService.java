package TelegramBot.Bot.authentification;

import TelegramBot.model.Database;
import TelegramBot.model.User;

import java.sql.SQLException;

// Класс должен реализовывать основную логику авторизации, регистрации, добавление в базу данных
// но все команды должен дёргать из вспомогательных классов

public class AuthService {
    private final Database database;


    public AuthService(Database database) {
        this.database = database;
    }

    public String registerUser(Long chatId, String username, String password) {
        try {
            if (database.existUser(chatId)) {
                return "Вы уже зарегистрированы!";
            } else if (!isValidUsername(username) || !isValidPassword(password)) {
                return "Неправильное имя пользователя или пароль";
            } else {
                User user = new User(chatId, username, password);
                database.addUser(user);
                return  "Регистрация прошла успешно!";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка регистрации. Попробуйте ещё раз";
        }
    }

    public String authorizeUser(Long chatId, String username, String password) {
        try {

            if (database.getUserById(chatId).isAuthorized()) {
                return "Вы уже авторизованы";
            } else if (database.authorizeUser(chatId, username, password)) {
                return "Вы успешно авторизованы!";
            } else {
                return "Неверное имя пользователя или пароль";
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
