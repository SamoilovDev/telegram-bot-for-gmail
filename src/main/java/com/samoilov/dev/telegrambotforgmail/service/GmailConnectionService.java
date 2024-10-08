package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;

public interface GmailConnectionService {

    String getAuthorizationUrl(Long chatId);

    Credential exchangeCode(String authCode, String redirectUri, Long chatId);

    Gmail createGmailService(Credential credential);

    void sendErrorResponse(Long chatId);

}
