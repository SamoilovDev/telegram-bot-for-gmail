package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailConnectionService;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.service.domain.UserService;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailBotService {

    private final UserService userService;

    private final GmailService gmailService;

    private final GmailConnectionService gmailConnectionService;

    private final ApplicationEventPublisher eventPublisher;

    public SendMessage getResponseMessage(UpdateInformationDto preparedUpdate) {
        Long chatId = preparedUpdate.getChatId();
        String message = preparedUpdate.getMessage();
        UserDto savedUser = userService.saveUser(preparedUpdate.getUser());
        CommandType currentCommand = CommandType.parseCommand(message.split("\\s+")[0]);

        userService.incrementCommandCounter(savedUser.getTelegramId());

        return switch (currentCommand) {
            case START -> this.getStartMessage(chatId, savedUser.getFirstName());
            case AUTHORIZE -> this.getAuthorizeMessage(chatId, gmailConnectionService.getAuthorizationUrl(chatId));
            case GMAIL -> this.getGmailMessage(chatId);
            case SEND -> null;
            case GET -> this.getEmailReceiveMessage(chatId, message);
            case INFO, COMMANDS, ERROR -> this.getSimpleResponseMessage(chatId, currentCommand);
        };
    }

    public SendMessage getStartMessage(Long chatId, String firstName) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.START.formatted(firstName))
                .replyMarkup(ButtonsUtil.getReplyKeyboard(false))
                .build();
    }

    public SendMessage getAuthorizeMessage(Long chatId, String authorizationUrl) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.AUTHORIZE)
                .replyMarkup(ButtonsUtil.getAuthorizeInlineKeyboard(authorizationUrl))
                .build();
    }

    public SendMessage getSimpleResponseMessage(Long chatId, CommandType commandType) {
        String message = switch (commandType) {
            case INFO -> MessagesUtil.INFO;
            case COMMANDS -> MessagesUtil.COMMANDS;
            case ERROR -> MessagesUtil.ERROR;
            default -> throw new IllegalStateException("Unexpected value: " + commandType);
        };

        return SendMessage.builder().chatId(chatId).text(message).build();
    }

    public SendMessage getGmailMessage(Long chatId) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.GMAIL)
                .replyMarkup(ButtonsUtil.getGmailMainKeyboard())
                .build();
    }

    public SendMessage getSendEmailMessage(Long chatId) {
        Gmail gmail = gmailConnectionService.getGmail(chatId);
        return SendMessage.builder().chatId(chatId).text(MessagesUtil.SEND).build();
    }

    public SendMessage getEmailReceiveMessage(Long chatId, String message) {
        String[] splitMessage = message.split("\\s+");
        if (splitMessage.length == 1) {
            return SendMessage
                    .builder()
                    .chatId(chatId)
                    .text(MessagesUtil.GET)
                    .replyMarkup(ButtonsUtil.getGmailMessageReceiveKeyboard())
                    .build();
        }

        Gmail gmail = gmailConnectionService.getGmail(chatId);
        String query = splitMessage.length > 2
                ? splitMessage[1].concat(splitMessage[2])
                : splitMessage[1];

        gmailService
                .getMessagesByQuery(gmail, query, chatId)
                .forEach(receivedEmail -> eventPublisher
                        .publishEvent(
                                SendMessage.builder().chatId(chatId).text(receivedEmail).build()
                        )
                );

        return SendMessage
                .builder()
                .chatId(chatId)
                .text(MessagesUtil.GET_FINISH)
                .replyMarkup(ButtonsUtil.getGmailMessageReceiveKeyboard())
                .build();
    }

}
