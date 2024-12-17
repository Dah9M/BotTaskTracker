package TelegramBot.model;

public enum TaskCategory {
    WORK, LIFE,  EDUCATION;

    public static boolean isValidCategory(String category) {
        try {
            valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
