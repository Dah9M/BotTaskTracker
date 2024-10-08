package TelegramBot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Keyboard {

    public InlineKeyboardMarkup setStartKeyboard() {
        InlineKeyboardMarkup startMenu = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton authButton = new InlineKeyboardButton();
        authButton.setText("Авторизоваться");
        authButton.setCallbackData("auth");

        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Зарегистрироваться");
        registerButton.setCallbackData("register");

        row1.add(authButton);
        row1.add(registerButton);
        rowsInline.add(row1);

        startMenu.setKeyboard(rowsInline);

        return startMenu;
    }

}
