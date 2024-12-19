package TelegramBot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.SQLException;

public class BotController extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(BotController.class);
    private final UpdateHandler updateHandler;

    public BotController() throws SQLException {
        this.updateHandler = new UpdateHandler(this);
        logger.info("BotController успешно инициализирован.");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            updateHandler.handleTextMessage(update);
            logger.debug("Обработано обновление от пользователя: {}", update.getMessage().getChatId());
        } catch (Exception e) {
            logger.error("Ошибка при обработке обновления: {}", update, e);
        }
    }

    @Override
    public String getBotUsername() {
        return "Task_TrackingBot";
    }

    @Override
    public String getBotToken() {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("BOT_TOKEN");
        logger.debug("Получен токен бота.");
        return token;
    }
}
