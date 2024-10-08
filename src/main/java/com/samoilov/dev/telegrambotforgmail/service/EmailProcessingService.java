package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.model.MessagePart;

import javax.mail.internet.MimeMessage;

public interface EmailProcessingService {
    String prepareMessagePart(MessagePart messagePart);

    MimeMessage prepareRawMessageToMime(String rawMessage, String fromEmail, Long chatId);
}
