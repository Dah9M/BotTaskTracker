package TelegramBot.model;

public class User {
    private Long chatId;
    // Добавьте другие поля, если необходимо

    public User(Long chatId) {
        this.chatId = chatId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}
