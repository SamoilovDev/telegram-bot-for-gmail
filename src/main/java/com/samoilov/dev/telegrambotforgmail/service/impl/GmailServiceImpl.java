package com.samoilov.dev.telegrambotforgmail.service.impl;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.service.EmailProcessingService;
import com.samoilov.dev.telegrambotforgmail.service.GmailService;
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
public class GmailServiceImpl implements GmailService {

    private final EmailProcessingService emailProcessingService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String GMAIL_USER_ID = "me";
    private static final String FORMAT = "full";

    @Override
    public List<String> getMessagesByQuery(Gmail userGmail, String query, Long chatId) {
        return this.getMessageListByQ(userGmail, query, chatId)
                .stream()
                .map(message -> this.getFullMessage(userGmail, message.getId(), chatId))
                .filter(message -> Objects.nonNull(message) && Objects.nonNull(message.getPayload()))
                .map(message -> emailProcessingService.prepareMessagePart(message.getPayload()))
                .toList();
    }

    @Override
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

    @Override
    public void sendEmail(Long chatId, String emailSample, Gmail userGmail) {
        String fromEmail = this.getEmailAddress(userGmail);
        MimeMessage mimeMessage = emailProcessingService.prepareRawMessageToMime(emailSample, fromEmail, chatId);

        this.sendEmailMessage(userGmail, mimeMessage, chatId);
    }

    private void sendEmailMessage(Gmail gmail, MimeMessage emailMessage, Long chatId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            emailMessage.writeTo(buffer);

            String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
            Message preparedMessage = new Message();

            preparedMessage.setRaw(encodedEmail);

            this.getMessagesFromGmail(gmail).send(GMAIL_USER_ID, preparedMessage).execute();
        } catch (IOException | MessagingException e) {
            eventPublisher.publishEvent(new SendMessage(
                    String.valueOf(chatId), "Error during sending message, please try again later."
            ));
            throw new GmailException(e);
        }
    }

    private List<Message> getMessageListByQ(Gmail gmail, String query, Long chatId) {
        try {
            return this.getMessagesFromGmail(gmail)
                    .list(GMAIL_USER_ID)
                    .setQ(query)
                    .execute()
                    .getMessages();
        } catch (IOException e) {
            eventPublisher.publishEvent(new SendMessage(
                    String.valueOf(chatId), "Error during getting messages, please try again later"
            ));
            throw new GmailException(e);
        }
    }

    private Message getFullMessage(Gmail gmail, String messageId, Long chatId) {
        try {
            return this.getMessagesFromGmail(gmail)
                    .get(GMAIL_USER_ID, messageId)
                    .setFormat(FORMAT)
                    .execute();
        } catch (IOException e) {
            eventPublisher.publishEvent(new SendMessage(
                    String.valueOf(chatId), "Error during getting messages, please try again later"
            ));
            throw new GmailException(e);
        }
    }

    private Gmail.Users.Messages getMessagesFromGmail(Gmail gmail) {
        return gmail.users().messages();
    }

}
