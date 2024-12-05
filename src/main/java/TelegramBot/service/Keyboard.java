package TelegramBot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

public class Keyboard {

    public ReplyKeyboardMarkup setStartKeyboard() {
        ReplyKeyboardMarkup startMenu = new ReplyKeyboardMarkup();
        startMenu.setResizeKeyboard(true);
        startMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Register");

        startMenu.setKeyboard(Arrays.asList(row1));

        return startMenu;
    }

    public ReplyKeyboardMarkup setMainKeyboard() {
        ReplyKeyboardMarkup mainMenu = new ReplyKeyboardMarkup();
        mainMenu.setResizeKeyboard(true);
        mainMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        row1.add("Add Task");
        row1.add("Update Task");

        row2.add("View Tasks");
        row2.add("Delete Task");

        row3.add("Help");

        mainMenu.setKeyboard(Arrays.asList(row1, row2, row3));

        return mainMenu;
    }

    public ReplyKeyboardMarkup setViewTasksKeyboard() {
        ReplyKeyboardMarkup tasksKeyboard = new ReplyKeyboardMarkup();
        tasksKeyboard.setResizeKeyboard(true);
        tasksKeyboard.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row1.add("All Tasks");
        row1.add("Waiting Tasks");

        row2.add("Active Tasks");
        row2.add("Completed Tasks");

        tasksKeyboard.setKeyboard(Arrays.asList(row1, row2));

        return tasksKeyboard;
    }
}
