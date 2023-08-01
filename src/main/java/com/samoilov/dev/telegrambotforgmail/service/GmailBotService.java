package com.samoilov.dev.telegrambotforgmail.service;

import com.samoilov.dev.telegrambotforgmail.dto.AuthenticationInfoDto;
import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailStorageService;
import com.samoilov.dev.telegrambotforgmail.service.domain.UserService;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailBotService {

    private final UserService userService;

    private final GmailService gmailService;

    private final GmailStorageService gmailStorageService;

    public SendMessage getResponseMessage(UpdateInformationDto preparedUpdate) {
        UserDto savedUser = userService.saveUser(preparedUpdate.getUser());
        Long chatId = preparedUpdate.getChatId();
        String authorizationUrl = gmailService.getAuthorizationUrl(chatId);
        AuthenticationInfoDto usersGmail = gmailStorageService.getGmailByChatId(chatId);

        userService.incrementCommandCounter(savedUser.getTelegramId());

        if (Objects.isNull(usersGmail)) {
            return this.getAuthorizeMessage(chatId, authorizationUrl);
        }

        return switch (CommandType.parseCommand(preparedUpdate.getMessage().split("\\s+")[0])) {
            case START -> this.getStartMessage(
                    chatId,
                    savedUser.getFirstName().concat(" ").concat(savedUser.getLastName()),
                    authorizationUrl
            );
            case INFO -> this.getInfoMessage(chatId);
            case COMMANDS -> this.getCommandsMessage(chatId);
            case ERROR -> this.getErrorMessage(chatId);
            case AUTHORIZE -> this.getAuthorizeMessage(chatId, authorizationUrl);
        };
    }

    public SendMessage getAuthorizeMessage(Long chatId, String authorizationUrl) {
        InlineKeyboardMarkup keyboardMarkup = ButtonsUtil.getButtonsByCommands(
                        List.of(
                                CommandType.COMMANDS.getCommand(),
                                CommandType.INFO.getCommand(),
                                CommandType.AUTHORIZE.getCommand().concat(authorizationUrl)
                        )
                );

        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.AUTHORIZE)
                .replyMarkup(keyboardMarkup)
                .build();
    }

    public SendMessage getStartMessage(Long chatId, String fullName, String authorizationUrl) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.START.formatted(fullName))
                .replyMarkup(
                        ButtonsUtil.getButtonsByCommands(
                                List.of(
                                        CommandType.AUTHORIZE.getCommand().concat(authorizationUrl),
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

}
