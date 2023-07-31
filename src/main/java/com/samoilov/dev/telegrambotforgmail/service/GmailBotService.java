package com.samoilov.dev.telegrambotforgmail.service;

import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.service.domain.UserService;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GmailBotService {

    private final UserService userService;

    private final GmailService gmailService;

    public SendMessage getResponseMessage(Update update) {
        UpdateInformationDto preparedUpdate = this.prepareUpdate(update);
        UserDto savedUser = userService.saveUser(preparedUpdate.getUser());
        String authorizationUrl = gmailService.getAuthorizationUrl(preparedUpdate.getChatId());

        userService.incrementCommandCounter(savedUser.getTelegramId());

        return switch (CommandType.parseCommand(preparedUpdate.getMessage().split("\\s+")[0])) {
            case START -> this.getStartMessage(
                    savedUser.getFirstName().concat(" ").concat(savedUser.getLastName()),
                    preparedUpdate.getChatId(),
                    authorizationUrl
            );
            case INFO -> this.getInfoMessage(preparedUpdate.getChatId());
            case COMMANDS -> this.getCommandsMessage(preparedUpdate.getChatId());
            case ERROR -> this.getErrorMessage(preparedUpdate.getChatId());
            case AUTHTORIZE -> this.getAuthorizeMessage(preparedUpdate.getChatId(), authorizationUrl);
        };
    }

    public SendMessage getAuthorizeMessage(Long chatId, String authorizationUrl) {
        InlineKeyboardMarkup keyboardMarkup = ButtonsUtil.getButtonsByCommands(
                        List.of(
                                CommandType.COMMANDS.getCommand(),
                                CommandType.INFO.getCommand(),
                                CommandType.AUTHTORIZE.getCommand().concat(authorizationUrl)
                        )
                );

        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.AUTHORIZE)
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public SendMessage getStartMessage(String fullName, Long chatId, String authorizationUrl) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.START.formatted(fullName))
                .replyMarkup(
                        ButtonsUtil.getButtonsByCommands(
                                List.of(
                                        CommandType.AUTHTORIZE.getCommand().concat(authorizationUrl),
                                        CommandType.COMMANDS.getCommand(),
                                        CommandType.INFO.getCommand()
                                )
                        )
                )
                .build();
    }

    public SendMessage getCommandsMessage(Long chatId) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.COMMANDS)
                .build();
    }

    public SendMessage getInfoMessage(Long chatId) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.INFO)
                .build();
    }

    public SendMessage getErrorMessage(Long chatId) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.ERROR)
                .build();
    }

    private UpdateInformationDto prepareUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            return UpdateInformationDto
                    .builder()
                    .chatId(message.getChatId())
                    .message(message.getText())
                    .user(message.getFrom())
                    .build();
        } else {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return UpdateInformationDto
                    .builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .message(callbackQuery.getData())
                    .user(callbackQuery.getFrom())
                    .build();
        }
    }

}
