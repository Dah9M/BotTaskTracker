package TelegramBot.task;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class TaskData {
    private long chatId;
    private String description;
    private Timestamp deadline;
    private String priority;
    private String status;
    private Timestamp creationDate;
    private int step = 0;

    public TaskData(Long chatId) {
        this.chatId = chatId;
    }

    public void nextStep() {
        step++;
    }
}
