package TelegramBot.Service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;
import java.util.ArrayList;

public class Keyboard {
    private long chatId;

    public SendMessage sendStartKeyboard(SendMessage sendMessage) {
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Выберите действие:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton authorizationButton = new InlineKeyboardButton();
        InlineKeyboardButton registrationButton = new InlineKeyboardButton();

        authorizationButton.setText("Авторизоваться");
        authorizationButton.setCallbackData("Authorization");
        registrationButton.setText("Регистрация");
        registrationButton.setCallbackData("Registration");

        rowInline1.add(authorizationButton);
        rowInline2.add(registrationButton);
        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);

        inlineKeyboardMarkup.setKeyboard(rowsInline);

        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        return sendMessage;
    }

    public long getChatId() {
        return this.chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}
