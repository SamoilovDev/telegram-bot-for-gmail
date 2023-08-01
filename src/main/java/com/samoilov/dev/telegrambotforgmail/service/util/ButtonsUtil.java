package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.AUTHORIZE;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.COMMANDS;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.GMAIL;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.INFO;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.START;

@UtilityClass
public class ButtonsUtil {

    private static final Map<String, InlineKeyboardButton> COMMAND_MESSAGE_BUTTONS = Map.of(
            START.getCommand(), InlineKeyboardButton.builder().text("Start").callbackData(START.getCommand()).build(),
            COMMANDS.getCommand(), InlineKeyboardButton.builder().text("Commands").callbackData(COMMANDS.getCommand()).build(),
            INFO.getCommand(), InlineKeyboardButton.builder().text("Info").callbackData(INFO.getCommand()).build(),
            AUTHORIZE.getCommand(), InlineKeyboardButton.builder().text("Authorize").callbackData(AUTHORIZE.getCommand()).build(),
            GMAIL.getCommand(), InlineKeyboardButton.builder().text("Gmail").callbackData(GMAIL.getCommand()).build()
    );

    private static final List<KeyboardButton> GMAIL_KEYBOARD_BUTTONS = List.of(
            KeyboardButton.builder().text("Get important mails").build(),
            KeyboardButton.builder().text("Get all mails").build(),
            KeyboardButton.builder().text("Get unread mails").build(),
            KeyboardButton.builder().text("Get starred mails").build()
    );

    public static InlineKeyboardMarkup getButtonsByCommands(List<String> command) {
        List<InlineKeyboardButton> foundedButtons = command
                .stream()
                .map(fullCommand -> {
                    String[] splitCommand = fullCommand.split("\\s+");
                    return switch (splitCommand.length) {
                        case 1 -> COMMAND_MESSAGE_BUTTONS.get(splitCommand[0]);
                        case 2 -> {
                            InlineKeyboardButton button = COMMAND_MESSAGE_BUTTONS.get(splitCommand[0]);
                            button.setUrl(splitCommand[1]);
                            yield button;
                        }
                        default -> null;
                    };
                })
                .filter(Objects::nonNull)
                .toList();
        int foundedButtonsSize = foundedButtons.size();

        return foundedButtonsSize > 1
                ? new InlineKeyboardMarkup(
                        List.of(
                                foundedButtons.subList(0, foundedButtonsSize / 2),
                                foundedButtons.subList(foundedButtonsSize / 2, foundedButtonsSize)
                        )
                )
                : InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(foundedButtons)
                        .build();
    }

    public static ReplyKeyboardMarkup getGmailKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = List.of(new KeyboardRow(), new KeyboardRow());

        for (int i = 0; i < GMAIL_KEYBOARD_BUTTONS.size(); i++) {
            keyboard
                    .get(i % 2 == 0 ? 0 : 1)
                    .add(GMAIL_KEYBOARD_BUTTONS.get(i));
        }

        replyKeyboardMarkup.setKeyboard(keyboard);

        return replyKeyboardMarkup;
    }

}
