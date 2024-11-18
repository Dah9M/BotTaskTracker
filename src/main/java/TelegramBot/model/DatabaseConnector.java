
package TelegramBot.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static DatabaseConnector instance;
    private Connection connection;

    private DatabaseConnector() {
        try {
            String url = "jdbc:postgresql://localhost:5432/task_tracker";
            String username = "bot_user";
            String password = "your_password";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnector getInstance() {
        if (instance == null) {
            instance = new DatabaseConnector();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
