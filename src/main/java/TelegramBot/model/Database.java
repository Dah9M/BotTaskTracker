package TelegramBot.model;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private Map<Long, User> users = new HashMap<>();

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public boolean existUser(Long id) {
        return users.containsKey(id);
    }

    public boolean authorizeUser(Long id, String username, String password) {
        User user = getUserById(id);

        if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
            user.authorize();
            return true;
        } else {
            return false;
        }
    }

    public int size() {
        return users.size();
    }
}
