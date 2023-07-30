package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class ButtonsUtil {

    private static final Map<String, InlineKeyboardButton> COMMAND_BUTTONS = Map.of(
            "/start", InlineKeyboardButton.builder().text("Start").callbackData("/start").build(),
            "/commands", InlineKeyboardButton.builder().text("Commands").callbackData("/commands").build(),
            "/info", InlineKeyboardButton.builder().text("Info").callbackData("/info").build(),
            "/authorize", InlineKeyboardButton.builder().text("Authorize").callbackData("/authorize").build()
    );

    public static InlineKeyboardMarkup getButtonsByCommands(List<String> command) {
        List<InlineKeyboardButton> foundedButtons = command
                .stream()
                .map(fullCommand -> {
                    String[] splitCommand = fullCommand.split("\\s+");
                    return switch (splitCommand.length) {
                        case 1 -> COMMAND_BUTTONS.get(splitCommand[0]);
                        case 2 -> {
                            InlineKeyboardButton button = COMMAND_BUTTONS.get(splitCommand[0]);
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

}
