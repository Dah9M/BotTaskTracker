package TelegramBot.task;

import TelegramBot.model.TaskCategory;
import TelegramBot.model.TaskPriority;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class TaskData {
    private int dbID;
    private long chatId;
    private String description;
    private Timestamp deadline;
    private TaskPriority priority;
    private String status;
    private TaskCategory category = null;
    private Timestamp creationDate;
    private int step = 0;

    private String selectedField;
    private String newValue;

    private boolean notified = false;
    private int deadlineNotificationCount = 0; // Новое поле

    public TaskData(Long chatId) {
        this.chatId = chatId;
    }

    public TaskData(int dbID, Long chatId, String description, Timestamp deadline, TaskPriority priority, String status, Timestamp creationDate, int deadlineNotificationCount) {
        this.dbID = dbID;
        this.chatId = chatId;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
        this.deadlineNotificationCount = deadlineNotificationCount;
    }

    public void setPriority(String priority) {
        if (TaskPriority.isValidPriority(priority)) {
            this.priority = TaskPriority.valueOf(priority.toUpperCase());
        } else {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }

    public void setCategory(String category) {
        if (TaskPriority.isValidPriority(category)) {
            this.category = TaskCategory.valueOf(category.toUpperCase());
        } else {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    public void nextStep() {
        step++;
    }

    @Override
    public String toString() {
        return "Task ID: " + dbID +
                "\nDescription: " + description +
                "\nDeadline: " + (deadline != null ? deadline.toString() : "No deadline set") +
                "\nPriority: " + priority +
                "\nStatus: " + status +
                "\nCreation Date: " + creationDate +
                "\nCategory: " + category;
    }
}
