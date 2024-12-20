package telegrambot.model;

public enum TaskPriority {
    LOW, MEDIUM, HIGH;

    public static boolean isValidPriority(String priority) {
        try {
            valueOf(priority.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
