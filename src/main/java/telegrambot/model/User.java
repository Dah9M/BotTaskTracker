package telegrambot.model;

import lombok.Getter;
import lombok.NonNull;

public class User {
    private int id;
    @Getter
    private Long chatId;

    public User(@NonNull Long chatId) {
        this.chatId = chatId;
    }

}
