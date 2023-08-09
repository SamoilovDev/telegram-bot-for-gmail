package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.enums.KeyboardType;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailCacheService;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailConnectionService;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.service.domain.UserService;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.AUTHORIZE;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.GET_FINISH;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.GMAIL;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.SEND_FINISH;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.SETTINGS;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.SETTINGS_DELETE;
import static com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil.SETTINGS_STATS;

@Service
@RequiredArgsConstructor
public class GmailBotService {

    private final UserService userService;

    private final GmailService gmailService;

    private final GmailCacheService gmailCacheService;

    private final GmailConnectionService gmailConnectionService;

    private final ApplicationEventPublisher eventPublisher;

    public SendMessage getResponseMessage(UpdateInformationDto preparedUpdate) {
        String message = preparedUpdate.getMessage();
        UserDto savedUser = userService.saveUser(preparedUpdate.getUser());
        CommandType currentCommand = CommandType.parseCommand(message.split("\\s+")[0]);

        userService.incrementCommandCounter(savedUser.getTelegramId());

        return switch (currentCommand) {
            case START, INFO, ERROR -> this.getSimpleMessage(
                    currentCommand, savedUser.getFirstName(), preparedUpdate.getChatId()
            );
            case AUTHORIZE, GMAIL -> this.getMessageWithButtons(
                    currentCommand, preparedUpdate.getChatId()
            );
            case SEND, GET -> this.getGmailProcessingMessage(
                    preparedUpdate.getChatId(), savedUser.getTelegramId(), message
            );
            case SETTINGS -> this.getSettingsMessage(
                    preparedUpdate.getChatId(), savedUser, message
            );
            default -> throw new IllegalStateException("Unexpected value: " + currentCommand);
        };
    }

    private SendMessage getSimpleMessage(CommandType currentCommand, String firstName, Long chatId) {
        String responseMessage = switch (currentCommand) {
            case START -> MessagesUtil.START.formatted(firstName);
            case INFO -> MessagesUtil.INFO;
            case ERROR -> MessagesUtil.ERROR;
            default -> throw new IllegalStateException("Unexpected value: " + currentCommand);
        };

        return this.createSendMessage(chatId, responseMessage, null);
    }

    private SendMessage getMessageWithButtons(CommandType currentCommand, Long chatId) {
        String responseMessage = currentCommand.equals(CommandType.AUTHORIZE)
                ? AUTHORIZE
                : GMAIL;
        ReplyKeyboard keyboard = currentCommand.equals(CommandType.AUTHORIZE)
                ? ButtonsUtil.getAuthorizeInlineKeyboard(gmailConnectionService.getAuthorizationUrl(chatId))
                : ButtonsUtil.getInlineKeyboard(KeyboardType.GMAIL_MAIN);

        return this.createSendMessage(chatId, responseMessage, keyboard);
    }

    private SendMessage getSettingsMessage(Long chatId, UserDto userDto, String message) {
        String[] splitMessage = message.split("\\s+");

        if (splitMessage.length == 1) {
            return this.createSendMessage(
                    chatId, SETTINGS, ButtonsUtil.getInlineKeyboard(KeyboardType.SETTINGS)
            );
        } else {
            String command = splitMessage[1];
            String preparedMessage = switch (command) {
                case "stats" -> SETTINGS_STATS.formatted(userDto.getFirstName(), 0, "ENABLED");
                case "delete" -> {
                    userService.disableUser(userDto.getTelegramId());
                    yield SETTINGS_DELETE.formatted(userDto.getFirstName());
                }
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };

            return this.createSendMessage(chatId, preparedMessage, null);
        }
    }

    private SendMessage getGmailProcessingMessage(Long chatId, Long telegramId, String message) {
        String[] splitMessage = message.split("\\s+", 2);
        boolean isSendProcess = splitMessage[0].equals(CommandType.SEND.getCommand());

        if (splitMessage.length > 1) {
            Gmail gmail = gmailCacheService.getGmail(chatId);

            userService.addEmailAddress(telegramId, gmailService.getEmailAddress(gmail));

            if (isSendProcess) {
                gmailService.sendEmail(chatId, splitMessage[1], gmail);
            } else {
                gmailService.getMessagesByQuery(gmail, splitMessage[1], chatId)
                        .forEach(receivedEmail -> eventPublisher.publishEvent(
                                this.createSendMessage(chatId, receivedEmail, null)
                        ));
            }
        }

        return this.createSendMessage(
                chatId,
                isSendProcess ? SEND_FINISH : GET_FINISH,
                isSendProcess ? null : ButtonsUtil.getGmailMessageReceiveKeyboard()
        );
    }

    private SendMessage createSendMessage(@NotNull Long chatId, String message, ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.MARKDOWN)
                .build();
    }

}
