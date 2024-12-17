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
        ReplyKeyboardMarkup criteriaMenu = new ReplyKeyboardMarkup();
        criteriaMenu.setResizeKeyboard(true);
        criteriaMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("By Status");
        row1.add("By Priority");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("By Category");

        criteriaMenu.setKeyboard(Arrays.asList(row1, row2));

        return criteriaMenu;
    }

    public ReplyKeyboardMarkup setCategoryKeyboard() {
        ReplyKeyboardMarkup categoryMenu = new ReplyKeyboardMarkup();
        categoryMenu.setResizeKeyboard(true);
        categoryMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Work");
        row1.add("Life");
        row1.add("Education");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("All");

        categoryMenu.setKeyboard(Arrays.asList(row1, row2));

        return categoryMenu;
    }

    public ReplyKeyboardMarkup setStatusKeyboard() {
        ReplyKeyboardMarkup statusMenu = new ReplyKeyboardMarkup();
        statusMenu.setResizeKeyboard(true);
        statusMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Active");
        row1.add("Waiting");
        row1.add("Completed");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("All");

        statusMenu.setKeyboard(Arrays.asList(row1, row2));

        return statusMenu;
    }

    public ReplyKeyboardMarkup setPriorityKeyboard() {
        ReplyKeyboardMarkup priorityMenu = new ReplyKeyboardMarkup();
        priorityMenu.setResizeKeyboard(true);
        priorityMenu.setOneTimeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row1.add("Low");
        row1.add("Medium");
        row1.add("High");

        row2.add("All");

        priorityMenu.setKeyboard(Arrays.asList(row1, row2));

        return priorityMenu;
    }
}
