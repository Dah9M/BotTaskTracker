package TelegramBot.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;

@Setter
@Getter
public class Task {
    private Long chatId;
    private String description;
    private Timestamp deadline;
    private String priority;
    private String status = "Waiting";
    private Timestamp creationDate;


    public Task(Long chatId, String description, Timestamp deadline, String priority, Timestamp creationDate) {
        this.chatId = chatId;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.creationDate = creationDate;
    }

}
