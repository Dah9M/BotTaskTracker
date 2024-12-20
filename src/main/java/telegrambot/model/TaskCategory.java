package telegrambot.model;

public enum TaskCategory {
    WORK, LIFE,  EDUCATION, NOT_SPECIFIED;

    public static boolean isValidCategory(String category) {
        try {
            valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
