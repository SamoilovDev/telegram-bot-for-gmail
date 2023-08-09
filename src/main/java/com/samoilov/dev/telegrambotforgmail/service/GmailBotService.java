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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Slf4j
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
            case START, INFO, ERROR -> {
                String responseMessage = switch (currentCommand) {
                    case START -> MessagesUtil.START.formatted(savedUser.getFirstName());
                    case INFO -> MessagesUtil.INFO;
                    case ERROR -> MessagesUtil.ERROR;
                    default -> throw new IllegalStateException("Unexpected value: " + currentCommand);
                };

                yield this.createSendMessage(preparedUpdate.getChatId(), responseMessage, null);
            }
            case AUTHORIZE, GMAIL -> {
                String responseMessage = currentCommand.equals(CommandType.AUTHORIZE)
                        ? MessagesUtil.AUTHORIZE
                        : MessagesUtil.GMAIL;
                ReplyKeyboard keyboard = currentCommand.equals(CommandType.AUTHORIZE)
                        ? ButtonsUtil.getAuthorizeInlineKeyboard(
                                gmailConnectionService.getAuthorizationUrl(preparedUpdate.getChatId())
                        )
                        : ButtonsUtil.getInlineKeyboard(KeyboardType.GMAIL_MAIN);

                yield this.createSendMessage(preparedUpdate.getChatId(), responseMessage, keyboard);
            }
            case SETTINGS -> this.getSettingsMessage(preparedUpdate.getChatId(), savedUser, message);
            case SEND -> this.getSendEmailMessage(preparedUpdate.getChatId(), savedUser.getTelegramId(), message);
            case GET -> this.getEmailReceiveMessage(preparedUpdate.getChatId(), savedUser.getTelegramId(), message);
        };
    }

    public SendMessage getSettingsMessage(Long chatId, UserDto userDto, String message) {
        String[] splitMessage = message.split("\\s+");

        if (splitMessage.length == 1) {
            return this.createSendMessage(
                    chatId, MessagesUtil.SETTINGS, ButtonsUtil.getInlineKeyboard(KeyboardType.SETTINGS)
            );
        } else {
            String command = splitMessage[1];
            String preparedMessage = switch (command) {
                case "stats" -> MessagesUtil.SETTINGS_STATS.formatted(userDto.getFirstName(), 0, "ENABLED");
                case "delete" -> {
                    userService.disableUser(userDto.getTelegramId());
                    yield MessagesUtil.SETTINGS_DELETE.formatted(userDto.getFirstName());
                }
                default -> throw new IllegalStateException("Unexpected value: " + command);
            };

            return this.createSendMessage(chatId, preparedMessage, null);
        }
    }

    public SendMessage getSendEmailMessage(Long chatId, Long telegramId, String message) {
        String[] splitMessage = message.split("\\s+", 2);
        if (splitMessage.length == 1) {
            return this.createSendMessage(chatId, MessagesUtil.SEND, null);
        } else {
            Gmail gmail = gmailCacheService.getGmail(chatId);

            this.setGmailAddressForUser(telegramId, gmail);
            gmailService.sendEmail(chatId, splitMessage[1], gmail);

            return this.createSendMessage(chatId, MessagesUtil.SEND_FINISH, null);
        }
    }

    public SendMessage getEmailReceiveMessage(Long chatId, Long telegramId, String message) {
        String[] splitMessage = message.split("\\s+", 2);

        if (splitMessage.length == 1) {
            return this.createSendMessage(chatId, MessagesUtil.GET, ButtonsUtil.getGmailMessageReceiveKeyboard());
        } else {
            Gmail gmail = gmailCacheService.getGmail(chatId);

            this.setGmailAddressForUser(telegramId, gmail);

            gmailService.getMessagesByQuery(gmail, splitMessage[1], chatId)
                    .forEach(
                            receivedEmail -> eventPublisher.publishEvent(
                                    this.createSendMessage(chatId, receivedEmail, null)
                            )
                    );

            return this.createSendMessage(
                    chatId, MessagesUtil.GET_FINISH, ButtonsUtil.getGmailMessageReceiveKeyboard()
            );
        }
    }

    private void setGmailAddressForUser(Long telegramId, Gmail gmail) {
        String email = gmailService.getEmailAddress(gmail);
        userService.addEmailAddress(telegramId, email);
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
