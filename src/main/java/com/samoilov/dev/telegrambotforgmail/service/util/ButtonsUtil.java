package com.samoilov.dev.telegrambotforgmail.service.util;

import com.samoilov.dev.telegrambotforgmail.enums.KeyboardType;
import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.AUTHORIZE;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.GET;
import static com.samoilov.dev.telegrambotforgmail.enums.CommandType.SEND;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.ANYWHERE;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.ATTACHMENT;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.IMPORTANT;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.READ;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.STARRED;
import static com.samoilov.dev.telegrambotforgmail.service.util.QueriesUtil.UNREAD;

@UtilityClass
public class ButtonsUtil {

    private static final List<InlineKeyboardButton> GMAIL_MAIN_BUTTONS = List.of(
            InlineKeyboardButton.builder().text("Get emails by category").callbackData(GET.getCommand()).build(),
            InlineKeyboardButton.builder().text("Send new email").callbackData(SEND.getCommand()).build()
    );

    private static final List<InlineKeyboardButton> SETTINGS_BUTTONS = List.of(
            InlineKeyboardButton.builder().text("Get my statistics").callbackData("/settings stats").build(),
            InlineKeyboardButton.builder().text("Delete my data and disable account").callbackData("/settings delete").build()
    );

    private static final List<InlineKeyboardButton> GMAIL_GET_MESSAGE_BUTTONS = List.of(
            InlineKeyboardButton.builder().text("All").callbackData(GET.getCommand().concat(ANYWHERE)).build(),
            InlineKeyboardButton.builder().text("Important").callbackData(GET.getCommand().concat(IMPORTANT)).build(),
            InlineKeyboardButton.builder().text("Starred").callbackData(GET.getCommand().concat(STARRED)).build(),
            InlineKeyboardButton.builder().text("Have attachment").callbackData(GET.getCommand().concat(ATTACHMENT)).build(),
            InlineKeyboardButton.builder().text("Unread").callbackData(GET.getCommand().concat(UNREAD)).build(),
            InlineKeyboardButton.builder().text("Read").callbackData(GET.getCommand().concat(READ)).build()
    );

    public static InlineKeyboardMarkup getGmailStartKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("Click to start working with Gmail")
                                        .callbackData("/gmail")
                                        .build()
                        )
                )
                .build();
    }

    public static InlineKeyboardMarkup getAuthorizeInlineKeyboard(String authorizeUri) {
        InlineKeyboardButton preparedAuthButton = InlineKeyboardButton.builder()
                .text("Authorize me")
                .callbackData(AUTHORIZE.getCommand())
                .url(authorizeUri)
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(preparedAuthButton))
                .build();
    }

    public static InlineKeyboardMarkup getInlineKeyboard(KeyboardType keyboardType) {
        List<InlineKeyboardButton> keyboardButtons = switch (keyboardType) {
            case GMAIL_MAIN -> GMAIL_MAIN_BUTTONS;
            case GMAIL_GET -> GMAIL_GET_MESSAGE_BUTTONS;
            case SETTINGS -> SETTINGS_BUTTONS;
        };
        int keyboardSize = keyboardButtons.size();

        return InlineKeyboardMarkup.builder()
                .keyboard(
                        keyboardSize > 1
                                ? List.of(
                                        keyboardButtons.subList(0, keyboardSize / 2),
                                        keyboardButtons.subList(keyboardSize / 2, keyboardSize)
                                )
                                : List.of(keyboardButtons)
                )
                .build();
    }

    public static InlineKeyboardMarkup getGmailMessageReceiveKeyboard() {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();

        for (InlineKeyboardButton gmailGetMessageButton : GMAIL_GET_MESSAGE_BUTTONS) {
            keyboardRow.add(gmailGetMessageButton);

            if (keyboardRow.size() == 2) {
                keyboard.add(keyboardRow);
                keyboardRow = new ArrayList<>();
            }
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
    }

}
