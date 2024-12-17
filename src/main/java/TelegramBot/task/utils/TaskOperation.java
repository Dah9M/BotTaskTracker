package TelegramBot.task.utils;

import TelegramBot.task.TaskData;

public interface TaskOperation {
    String startOperation(Long chatId);
    String processInput(Long chatId, String input);
    void clearOperationData(Long chatId);
    boolean isInProgress(Long chatId);
    public TaskData getTaskData(Long chatId);
}

