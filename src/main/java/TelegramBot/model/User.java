package TelegramBot.model;

public class User {
    private int id;
    private Long chatId;
    private String username;
    private String password;
    private boolean isAuthorized;

    public User(Long chatId, String username, String password) {
        this.chatId = chatId;
        this.username = username;
        this.password = password;
        this.isAuthorized = false;
    }

    public Long getId() { return chatId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isAuthorized() { return isAuthorized; }
    public void authorize() { this.isAuthorized = true; }

}
