package TelegramBot.model;

import lombok.Getter;

public class User {
    private int id;
    @Getter
    private Long chatId;

    public User(Long chatId) {
        this.chatId = chatId;

    }

}
