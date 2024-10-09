package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import org.springframework.context.event.EventListener;

public interface GmailAuthorizationService {

    @EventListener(AuthenticationInfoDto.class)
    void handleAuthenticationInfo(AuthenticationInfoDto authenticationInfoDto);

    String getAuthorizationUrl(Long chatId);

    Credential exchangeAuthorizationCode(String authCode, String redirectUri, Long chatId);

    Gmail authorizeGmail(Credential credential);

    void sendAuthorizationFailedMessage(Long chatId);

}
