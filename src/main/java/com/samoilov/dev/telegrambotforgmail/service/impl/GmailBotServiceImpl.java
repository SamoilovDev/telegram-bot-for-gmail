package com.samoilov.dev.telegrambotforgmail.service.impl;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.service.GmailBotService;
import com.samoilov.dev.telegrambotforgmail.service.GmailCacheService;
import com.samoilov.dev.telegrambotforgmail.service.GmailConnectionService;
import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.enums.CommandType;
import com.samoilov.dev.telegrambotforgmail.store.enums.KeyboardType;
import com.samoilov.dev.telegrambotforgmail.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.util.MessagesUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Objects;

import static com.samoilov.dev.telegrambotforgmail.util.MessagesUtil.*;

@Service
@RequiredArgsConstructor
public class GmailBotServiceImpl implements GmailBotService {

    private final UserServiceImpl userService;

    private final GmailServiceImpl gmailService;

    private final GmailCacheService gmailCacheService;

    private final GmailConnectionService gmailConnectionService;

    private final ApplicationEventPublisher eventPublisher;

    private static final String UNEXPECTED_VALUE = "Unexpected value: %s";
    private static final String ABSENT_INFORMATION = "information is absent";

    @Override
    public SendMessage getResponseMessage(UpdateInformationDto preparedUpdate) {
        Long chatId = preparedUpdate.getChatId();
        String message = preparedUpdate.getMessage().replaceAll("@\\w+\\s", "");
        UserDto savedUser = userService.saveUser(preparedUpdate.getTelegramUser());
        CommandType currentCommand = CommandType.parseCommand(message.split("\\s+")[0]);

        userService.incrementCommandCounter(savedUser.getTelegramId());
        userService.addChatId(savedUser.getTelegramId(), chatId);

        return switch (currentCommand) {
            case START, INFO, ERROR -> this.getSimpleMessage(currentCommand, savedUser.getFirstName(), chatId);
            case AUTHORIZE, GMAIL -> this.getMessageWithButtons(currentCommand, chatId);
            case SEND, GET -> this.getGmailProcessingMessage(chatId, savedUser.getTelegramId(), message);
            case SETTINGS -> this.getSettingsMessage(chatId, savedUser, message);
            default -> throw new IllegalStateException(UNEXPECTED_VALUE.formatted(currentCommand));
        };
    }

    private SendMessage getSimpleMessage(CommandType currentCommand, String firstName, Long chatId) {
        String responseMessage = switch (currentCommand) {
            case START -> MessagesUtil.START.formatted(firstName);
            case INFO -> MessagesUtil.INFO;
            case ERROR -> MessagesUtil.ERROR;
            default -> throw new IllegalStateException(UNEXPECTED_VALUE.formatted(currentCommand));
        };

        return this.createSendMessage(chatId, responseMessage, null);
    }

    private SendMessage getMessageWithButtons(CommandType currentCommand, Long chatId) {
        String responseMessage = currentCommand.equals(CommandType.AUTHORIZE) ? AUTHORIZE : GMAIL;
        ReplyKeyboard keyboard = currentCommand.equals(CommandType.AUTHORIZE)
                ? ButtonsUtil.getAuthorizeInlineKeyboard(gmailConnectionService.getAuthorizationUrl(chatId))
                : ButtonsUtil.getInlineKeyboard(KeyboardType.GMAIL_MAIN);

        return this.createSendMessage(chatId, responseMessage, keyboard);
    }

    private SendMessage getSettingsMessage(Long chatId, UserDto userDto, String message) {
        String[] splitMessage = message.split("\\s+");

        if (splitMessage.length == 1) {
            return this.createSendMessage(chatId, SETTINGS, ButtonsUtil.getInlineKeyboard(KeyboardType.SETTINGS));
        }

        String command = splitMessage[1];
        String preparedMessage = switch (command) {
            case "stats" -> this.prepareStatsMessage(userDto);
            case "delete" -> {
                userService.disableUser(userDto.getTelegramId());
                yield SETTINGS_DELETE.formatted(userDto.getFirstName());
            }
            default -> throw new IllegalStateException(UNEXPECTED_VALUE.formatted(command));
        };

        return this.createSendMessage(chatId, preparedMessage, null);

    }

    private SendMessage getGmailProcessingMessage(Long chatId, Long telegramId, String message) {
        Gmail gmail = gmailCacheService.getGmail(chatId);
        String[] splitMessage = message.split("\\s+", 2);
        boolean isSendProcess = splitMessage[0].equals(CommandType.SEND.getCommand());

        if (splitMessage.length == 1) {
            return this.createSendMessage(
                    chatId,
                    isSendProcess ? SEND : GET,
                    isSendProcess
                            ? ButtonsUtil.getGmailSendMessageTemplateKeyboard()
                            : ButtonsUtil.getGmailMessageReceiveKeyboard()
            );
        }

        userService.addEmailAddress(telegramId, gmailService.getEmailAddress(gmail));

        if (isSendProcess) {
            gmailService.sendEmail(chatId, splitMessage[1], gmail);
        } else {
            gmailService
                    .getMessagesByQuery(gmail, splitMessage[1], chatId)
                    .forEach(receivedEmail -> eventPublisher.publishEvent(
                            this.createSendMessage(chatId, receivedEmail, null)
                    ));
        }

        return this.createSendMessage(
                chatId,
                isSendProcess ? SEND_FINISH : GET_FINISH,
                isSendProcess
                        ? ButtonsUtil.getGmailStartKeyboard()
                        : ButtonsUtil.getGmailMessageReceiveKeyboard()
        );
    }

    private String prepareStatsMessage(UserDto userDto) {
        String firstCommandTime = Objects.isNull(userDto.getCreateDate())
                ? ABSENT_INFORMATION
                : userDto.getCreateDate().toString().replace('T', ' ');
        StringBuilder emails = new StringBuilder("[");

        if (Objects.isNull(userDto.getEmails()) || userDto.getEmails().isEmpty()) {
            emails.delete(0, 1)
                    .append(ABSENT_INFORMATION);
        } else {
            userDto.getEmails()
                    .forEach(email -> emails.append(email).append(", "));
            emails.delete(emails.length() - 2, emails.length())
                    .append("]");
        }

        return SETTINGS_STATS.formatted(
                userDto.getTelegramId(),
                userDto.getFirstName(),
                Objects.isNull(userDto.getLastName()) ? ABSENT_INFORMATION : userDto.getLastName(),
                Objects.isNull(userDto.getUserName()) ? ABSENT_INFORMATION : userDto.getUserName(),
                firstCommandTime,
                userDto.getCommandCounter(),
                userDto.getActiveType(),
                emails.toString()
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
