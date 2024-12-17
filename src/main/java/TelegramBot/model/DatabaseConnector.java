package TelegramBot.model;

import TelegramBot.utils.LoggerFactoryUtil;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    public Connection connect() throws SQLException {
        LoggerFactoryUtil.logError("Ошибка при попытке подключения к базе данных", new SQLException());
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
