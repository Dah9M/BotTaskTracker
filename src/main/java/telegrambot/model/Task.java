package telegrambot.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Task {
    private Long chatId;
    private String description;
    private Timestamp deadline;
    private TaskPriority priority;
    private String status = "Waiting";
    private Timestamp creationDate;
    private TaskCategory category = null;
    private boolean notified = false;
    private int deadlineNotificationCount = 0;

    public Task(Long chatId, String description, Timestamp deadline, TaskPriority priority, Timestamp creationDate, int deadlineNotificationCount, TaskCategory category) {
        this.chatId = chatId;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.creationDate = creationDate;
        this.deadlineNotificationCount = deadlineNotificationCount;
        this.category = category;
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
}
