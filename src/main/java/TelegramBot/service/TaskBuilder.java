package TelegramBot.service;

public class TaskBuilder {
    public String getTaskData(String callbackData) {
        switch (callbackData) {
            case "task1":
                return "Информация о задаче 1.";
            case "task2":
                return "Информация о задаче 2.";
            default:
                return "Неизвестная задача.";
        }
    }
}
