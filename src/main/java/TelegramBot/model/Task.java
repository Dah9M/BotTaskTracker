package TelegramBot.model;

import java.sql.Time;
import java.sql.Timestamp;

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

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeadline(Timestamp deadline) {
        this.deadline = deadline;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getChatId() {
        return chatId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getDeadline() {
        return deadline;
    }

}
