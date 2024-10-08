package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.Gmail;

import java.util.List;

public interface GmailService {

    List<String> getMessagesByQuery(Gmail userGmail, String query, Long chatId);

    String getEmailAddress(Gmail gmail);

    void sendEmail(Long chatId, String emailSample, Gmail userGmail);

}
