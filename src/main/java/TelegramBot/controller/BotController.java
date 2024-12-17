package TelegramBot.controller;

import TelegramBot.utils.LoggerFactoryUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.SQLException;

public class BotController extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;

    public BotController() throws SQLException {
        LoggerFactoryUtil.logError("Ошибка в конструкторе BotController", new SQLException());
        this.updateHandler = new UpdateHandler(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateHandler.handleTextMessage(update);
    }

    @Override
    public String getBotUsername() {
        return "Task_TrackinBot";
    }

    @Override
    public String getBotToken() {
        Dotenv dotenv = Dotenv.load();
        return dotenv.get("BOT_TOKEN");
    }
}
