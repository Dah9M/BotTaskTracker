package TelegramBot.task;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;

@Setter
@Getter
public class TaskData {
    private long dbID;
    private long chatId;
    private String description;
    private Timestamp deadline;
    private String priority;
    private String status;
    private Timestamp creationDate;
    private int step = 0;


    private String selectedField;
    private String newValue;

    public TaskData(Long chatId) {
        this.chatId = chatId;
    }
    public TaskData(Long dbID, Long chatId, String description, Timestamp deadline, String priority, String status, Timestamp creationDate) {
        this.dbID = dbID;
        this.chatId = chatId;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
    }

    public void nextStep() {
        step++;
    }
}
