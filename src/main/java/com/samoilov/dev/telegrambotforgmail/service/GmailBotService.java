package com.samoilov.dev.telegrambotforgmail.service;

import com.samoilov.dev.telegrambotforgmail.component.UserCredentialsMapper;
import com.samoilov.dev.telegrambotforgmail.dto.GmailDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.service.domain.UserService;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailBotService {

    private final UserService userService;

    private final GmailService gmailService;

    private final UserCredentialsMapper userCredentialsMapper;

    public SendMessage getResponseMessage(Update update) {
        String message = update.hasMessage()
                ? update.getMessage().getText()
                : update.getCallbackQuery().getData();
        Long chatId = update.hasMessage()
                ? update.getMessage().getChatId()
                : update.getCallbackQuery().getMessage().getChatId();

        UserDto savedUser = userCredentialsMapper.mapEntityToDto(
                userService.saveNewUser(
                        update.hasMessage()
                                ? update.getMessage().getFrom()
                                : update.getCallbackQuery().getFrom()
                )
        );
        String firstName = savedUser.getFirstName();

        GmailDto gmailDto = gmailService.getGmail(String.valueOf(savedUser.getTelegramId()));

        log.info("Chat id: {}", chatId);

        userService.incrementCommandCounter(savedUser.getTelegramId());

        return switch (CommandType.parseCommand(message.split("\\s+")[0])) {
            case START -> this.getStartMessage(
                    firstName.concat(" ").concat(savedUser.getLastName()),
                    chatId,
                    gmailDto.getAuthorizationUrl()
            );
            case INFO -> this.getInfoMessage(chatId);
            case COMMANDS -> this.getCommandsMessage(chatId);
            case ERROR -> this.getErrorMessage(chatId);
            case AUTHTORIZE -> this.getAuthorizeMessage(chatId, gmailDto.getAuthorizationUrl());
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

}
