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
        InlineKeyboardButton registerButton = new InlineKeyboardButton();
        registerButton.setText("Register");
        registerButton.setCallbackData("register");

        row1.add(registerButton);
        rowsInline.add(row1);

        startMenu.setKeyboard(rowsInline);

        return startMenu;
    }

    public InlineKeyboardMarkup setMainKeyboard() {
        InlineKeyboardMarkup mainMenu = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton addTaskButton = new InlineKeyboardButton();
        InlineKeyboardButton updateTaskButton = new InlineKeyboardButton();

        addTaskButton.setText("Add Task");
        updateTaskButton.setText("Update Task");
        addTaskButton.setCallbackData("addTask");
        updateTaskButton.setCallbackData("updateTask");

        row1.add(addTaskButton);
        row1.add(updateTaskButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton viewTasksButton = new InlineKeyboardButton();
        InlineKeyboardButton deleteTaskButton = new InlineKeyboardButton();

        viewTasksButton.setText("View Tasks");
        deleteTaskButton.setText("Delete Task");
        viewTasksButton.setCallbackData("viewTasks");
        deleteTaskButton.setCallbackData("deleteTask");

        row2.add(viewTasksButton);
        row2.add(deleteTaskButton);

        List<InlineKeyboardButton> row3 = new ArrayList<>();

        InlineKeyboardButton help = new InlineKeyboardButton();

        help.setText("Help");
        help.setCallbackData("help");

        row3.add(help);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);

        mainMenu.setKeyboard(rowsInline);

        return mainMenu;
    }

    public InlineKeyboardMarkup setViewTasksKeyboard() {
        InlineKeyboardMarkup tasksKeyboard = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        InlineKeyboardButton allTasksButton = new InlineKeyboardButton();
        InlineKeyboardButton waitingTasksButton = new InlineKeyboardButton();
        InlineKeyboardButton activeTasksButton = new InlineKeyboardButton();
        InlineKeyboardButton completedTasksButton = new InlineKeyboardButton();

        allTasksButton.setText("All Tasks.");
        allTasksButton.setCallbackData("allTasks");
        allTasksButton.setText("Waiting Tasks.");
        allTasksButton.setCallbackData("waitingTasks");
        allTasksButton.setText("Active Tasks.");
        allTasksButton.setCallbackData("activeTasks");
        allTasksButton.setText("Completed Tasks.");
        allTasksButton.setCallbackData("completedTasks");

        row1.add(allTasksButton);
        row2.add(waitingTasksButton);
        row3.add(activeTasksButton);
        row4.add(completedTasksButton);

        rowsInline.add(row1);
        rowsInline.add(row2);
        rowsInline.add(row3);
        rowsInline.add(row4);

        tasksKeyboard.setKeyboard(rowsInline);

        return tasksKeyboard;
    }
}
