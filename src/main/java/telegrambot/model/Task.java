package telegrambot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
public class Task {
    private Long chatId;
    private String description;
    private Timestamp deadline;
    private TaskPriority priority;
    private String status = "Waiting";
    private Timestamp creationDate;
    private TaskCategory category;
    private boolean notified = false;
    private int deadlineNotificationCount = 0;

    public Task(@NonNull Long chatId, @NonNull String description, @NonNull Timestamp deadline, @NonNull TaskPriority priority,
                Timestamp creationDate, int deadlineNotificationCount, TaskCategory category) {
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
