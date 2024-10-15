package TelegramBot.model;

public class Task {
    private String taskText;
    private String deadline;

    public String getTaskText() {
        return this.taskText;
    }

    public void setTaskText(String taskText) {
        this.taskText = taskText;
    }

    public String getDeadline() {
        return this.deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }
}
