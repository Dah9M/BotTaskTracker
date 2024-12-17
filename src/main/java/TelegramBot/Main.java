package TelegramBot;

import TelegramBot.controller.BotController;
import TelegramBot.utils.LoggerFactoryUtil;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws TelegramApiException, SQLException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        BotController botController = new BotController();
        telegramBotsApi.registerBot(botController);
        LoggerFactoryUtil.logInfo("Бот успешно начал работу.");
    }
}