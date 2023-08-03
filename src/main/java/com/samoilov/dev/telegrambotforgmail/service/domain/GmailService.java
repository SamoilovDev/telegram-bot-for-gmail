package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.samoilov.dev.telegrambotforgmail.component.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final GmailStorageService gmailStorageService;

    private final ApplicationEventPublisher eventPublisher;

    private final InformationMapper informationMapper;

    public List<String> getMessagesByQuery(Gmail userGmail, String query, Long chatId) {
        try {
            ListMessagesResponse messagesList = userGmail
                    .users()
                    .messages()
                    .list("me")
                    .setQ(query)
                    .execute();

            return messagesList
                    .getMessages()
                    .stream()
                    .filter(message -> Objects.nonNull(message) && Objects.nonNull(message.getPayload()))
                    .map(message -> informationMapper
                            .mapGmailPayloadToEmailMessageDto(message.getPayload())
                            .toString()
                    )
                    .toList();
        } catch (IOException e) {
            eventPublisher.publishEvent(
                    SendMessage
                            .builder()
                            .chatId(chatId)
                            .text("Error while getting messages, please try again later")
                            .build()
            );
            throw new GmailException(e);
        }
    }

}
