package telegrambot.controller;

import telegrambot.model.BotUtils;
import telegrambot.model.Commands;
import telegrambot.model.TaskCategory;
import telegrambot.service.MessageSender;
import telegrambot.task.TaskController;
import telegrambot.model.TaskPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

public class UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);
    private final MessageSender messageSender;
    private final BotUtils botUtils;
    private final Commands commands;

    public UpdateHandler(TelegramLongPollingBot bot) {
        this.messageSender = new MessageSender(bot);
        this.botUtils = BotUtils.getInstance(messageSender);
        this.commands = Commands.getInstance(botUtils);
    }

    public void handleTextMessage(Update update) {
        Long currentChatId = update.getMessage().getChatId();
        String currentInput = update.getMessage().getText();
        TaskController taskController = botUtils.getTaskController();

        messageSender.setCurrentChatId(currentChatId);
        logger.debug("Получено сообщение от пользователя {}: {}", currentChatId, currentInput);

        try {
            if (taskController.isTaskInProgress()) {
                if (taskController.isWaitingForPriorityInput()) {
                    if (!TaskPriority.isValidPriority(currentInput)) {
                        messageSender.sendMessage("Invalid priority. Please enter Low, Medium, or High.");
                        logger.warn("Пользователь {} ввёл некорректный приоритет: {}", currentChatId, currentInput);
                    } else {
                        String response = taskController.handleTaskInput(currentInput);
                        messageSender.sendMessage(response);
                        logger.info("Пользователь {} завершил ввод приоритета задачи.", currentChatId);
                    }
                } else {
                    String response = taskController.handleTaskInput(currentInput);
                    messageSender.sendMessage(response);
                    logger.info("Пользователь {} продолжил процесс добавления задачи.", currentChatId);
                }
            } else if (taskController.isUpdateInProgress()) {
                if (taskController.isWaitingForCategoryInput()) { // Проверка ожидания ввода категории
                    if (!TaskCategory.isValidCategory(currentInput)) {
                        messageSender.sendMessage("Invalid category. Please enter Work, Life, or Education.");
                        logger.warn("Пользователь {} ввёл некорректную категорию: {}", currentChatId, currentInput);
                    } else {
                        String response = taskController.handleUpdateInput(currentInput);
                        messageSender.sendMessage(response);
                        logger.info("Пользователь {} завершил ввод категории для обновления задачи.", currentChatId);
                    }
                } else {
                    String response = taskController.handleUpdateInput(currentInput);
                    messageSender.sendMessage(response);
                    logger.info("Пользователь {} продолжил процесс обновления задачи.", currentChatId);
                }

            } else if (taskController.isDeleteInProgress()) {
                String response = taskController.handleDeleteInput(currentInput);
                messageSender.sendMessage(response);
                logger.info("Пользователь {} обработал процесс удаления задачи.", currentChatId);
            } else {
                Runnable command = commands.getCommand(currentInput);
                if (command != null) {
                    command.run();
                    logger.info("Выполнена команда '{}' от пользователя {}.", currentInput, currentChatId);
                } else {
                    messageSender.sendMessage("Unknown command.");
                    logger.warn("Неизвестная команда '{}' от пользователя {}.", currentInput, currentChatId);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при обработке сообщения от пользователя {}: {}", currentChatId, currentInput, e);
            messageSender.sendMessage("An error occurred while processing your request.");
        }
    }
}
