package telegrambot.controller;

import lombok.extern.slf4j.Slf4j;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.SQLException;

@Slf4j
public class BotController extends TelegramLongPollingBot {
    private final UpdateHandler updateHandler;

    public BotController() throws SQLException {
        this.updateHandler = new UpdateHandler(this);
        log.info("BotController успешно инициализирован.");
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            updateHandler.handleTextMessage(update);
            log.debug("Обработано обновление от пользователя: {}", update.getMessage().getChatId());
        } catch (Exception e) {
            log.error("Ошибка при обработке обновления: {}", update, e);
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
        log.debug("Получен токен бота.");
        return token;
    }
}
