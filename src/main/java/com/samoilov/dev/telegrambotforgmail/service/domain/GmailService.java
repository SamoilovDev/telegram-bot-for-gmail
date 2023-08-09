package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final EmailProcessingService emailProcessingService;

    private final ApplicationEventPublisher eventPublisher;

    private static final String GMAIL_USER_ID = "me";

    private static final String FORMAT = "full";

    public List<String> getMessagesByQuery(Gmail userGmail, String query, Long chatId) {
        return this.getMessageListByQ(userGmail, query, chatId)
                .stream()
                .map(message -> this.getFullMessage(userGmail, message.getId()))
                .filter(message -> Objects.nonNull(message) && Objects.nonNull(message.getPayload()))
                .map(message -> emailProcessingService.prepareMessagePart(message.getPayload()))
                .toList();
    }

    public String getEmailAddress(Gmail gmail) {
        try {
            return gmail.users()
                    .getProfile(GMAIL_USER_ID)
                    .execute()
                    .getEmailAddress();
        } catch (IOException e) {
            throw new GmailException(e);
        }
    }

    public void sendEmail(Long chatId, String message, Gmail userGmail) {
        String fromEmail = this.getEmailAddress(userGmail);
        MimeMessage mimeMessage = emailProcessingService.prepareRawMessageToMime(message, fromEmail, chatId);
        this.sendEmailMessage(userGmail, mimeMessage, chatId);
    }

    private void sendEmailMessage(Gmail userGmail, MimeMessage emailMessage, Long chatId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            emailMessage.writeTo(buffer);

            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
            Message preparedMessage = new Message();

            preparedMessage.setRaw(encodedEmail);

            userGmail.users()
                    .messages()
                    .send(GMAIL_USER_ID, preparedMessage)
                    .execute();
        } catch (IOException | MessagingException e) {
            eventPublisher.publishEvent(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text("Error during sending message, please try again later.")
                            .build()
            );
            throw new GmailException(e);
        }
    }

    private List<Message> getMessageListByQ(Gmail userGmail, String query, Long chatId) {
        try {
            return userGmail.users()
                    .messages()
                    .list(GMAIL_USER_ID)
                    .setQ(query)
                    .execute()
                    .getMessages();
        } catch (IOException e) {
            eventPublisher.publishEvent(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text("Error while getting messages, please try again later")
                            .build()
            );
            throw new GmailException(e);
        }
    }

    private Message getFullMessage(Gmail userGmail, String messageId) {
        try {
            return userGmail.users()
                    .messages()
                    .get(GMAIL_USER_ID, messageId)
                    .setFormat(FORMAT)
                    .execute();
        } catch (IOException e) {
            throw new GmailException(e);
        }
    }

}
