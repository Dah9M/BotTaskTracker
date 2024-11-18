package TelegramBot.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private static UserRepository instance;
    private final DatabaseConnector databaseConnector;

    private UserRepository(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    public static synchronized UserRepository getInstance(DatabaseConnector databaseConnector) {
        if (instance == null) {
            instance = new UserRepository(databaseConnector);
        }
        return instance;
    }

    public User getUserByChatId(Long chatId) throws SQLException {
        Connection connection = databaseConnector.getConnection();
        String query = "SELECT * FROM users WHERE chat_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, chatId);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            // Предполагая, что у вас есть конструктор User с соответствующими параметрами
            return new User(resultSet.getLong("chat_id"));
        }
        return null;
    }

    public void registerUser(User user) throws SQLException {
        Connection connection = databaseConnector.getConnection();
        String query = "INSERT INTO users (chat_id) VALUES (?)";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, user.getChatId());
        statement.executeUpdate();
    }

}
