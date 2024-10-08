package TelegramBot.controller;

import TelegramBot.service.UpdateHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotController extends TelegramLongPollingBot {

    private final UpdateHandler updateHandler;

    public BotController() {
        this.updateHandler = new UpdateHandler(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateHandler.updateHandle(update);
    }

    @Override
    public String getBotUsername() {
        return "Task_TrackinBot";
    }

    @Override
    public String getBotToken() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("bot.properties")) {
            if (input == null) {
                System.err.println("Unable to find telegram.properties");
                return null;
            }
            properties.load(input);
            return properties.getProperty("bot.token");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
