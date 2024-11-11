package TelegramBot.task.utils;

public interface TaskOperation {
    String startOperation(Long chatId);
    String processInput(Long chatId, String input);
    boolean isOperationCompleted(Long chatId);
    void clearOperationData(Long chatId);
}
