package telegrambot.task;

import lombok.NonNull;
import lombok.ToString;
import telegrambot.model.TaskCategory;
import telegrambot.model.TaskPriority;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@ToString
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
    private int deadlineNotificationCount = 0;

    public TaskData(@NonNull int dbID, @NonNull Long chatId, @NonNull String description, @NonNull Timestamp deadline, TaskPriority priority, String status, Timestamp creationDate, int deadlineNotificationCount, TaskCategory category) {
        this.dbID = dbID;
        this.chatId = chatId;
        this.description = description;
        this.deadline = deadline;
        this.priority = priority;
        this.status = status;
        this.creationDate = creationDate;
        this.deadlineNotificationCount = deadlineNotificationCount;
        this.category = category;
    }

    public void setPriority(String priority) {
        if (TaskPriority.isValidPriority(priority)) {
            this.priority = TaskPriority.valueOf(priority);
        } else {
            throw new IllegalArgumentException("Invalid priority: " + priority);
        }
    }

    public void setCategory(String category) {
        if (TaskCategory.isValidCategory(category)) {
            this.category = TaskCategory.valueOf(category.toUpperCase());
        } else {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    public void nextStep() {
        step++;
    }
}
