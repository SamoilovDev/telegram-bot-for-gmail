package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import com.samoilov.dev.telegrambotforgmail.exception.AuthorizationUrlCreatingException;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.util.MessagesUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.samoilov.dev.telegrambotforgmail.util.MessagesUtil.IMPOSSIBLE_TO_AUTHORIZE_NOW;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailConnectionService {

    private final ApplicationEventPublisher eventPublisher;
    private final GoogleClientSecrets googleClientSecrets;
    private final GoogleProperties googleProperties;
    private final NetHttpTransport netHttpTransport;
    private final JsonFactory jsonFactory;
    private final List<String> scopes;

    private File tokenStore;

    @PostConstruct
    public void createTokenStore() throws IOException {
        tokenStore = new File(googleProperties.getTokensDirectoryPath());

        if (!tokenStore.exists()) {
            Files.createDirectories(tokenStore.toPath());
        }
    }

    @PreDestroy
    private void deleteTokenStore() throws IOException {
        Files.deleteIfExists(tokenStore.toPath());
    }

    public String getAuthorizationUrl(Long chatId) {
        try {
            return new GoogleAuthorizationCodeFlow.Builder(netHttpTransport, jsonFactory, googleClientSecrets, scopes)
                    .setDataStoreFactory(new FileDataStoreFactory(tokenStore))
                    .setAccessType("offline")
                    .build()
                    .newAuthorizationUrl()
                    .setRedirectUri(googleProperties.getRedirectUri().concat(String.valueOf(chatId)))
                    .build();
        } catch (IOException | NullPointerException e) {
            eventPublisher.publishEvent(new SendMessage(String.valueOf(chatId), IMPOSSIBLE_TO_AUTHORIZE_NOW));
            throw new AuthorizationUrlCreatingException(e);
        }
    }


    @SuppressWarnings("deprecation")
    public Credential exchangeCode(String authCode, String redirectUri, Long chatId) {
        try {
            GoogleClientSecrets.Details details = googleClientSecrets.getDetails();
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    netHttpTransport,
                    jsonFactory,
                    googleProperties.getOauthUrl(),
                    details.getClientId(),
                    details.getClientSecret(),
                    authCode,
                    redirectUri
            ).execute();

            return new GoogleCredential.Builder()
                    .setClientSecrets(googleClientSecrets)
                    .setJsonFactory(jsonFactory)
                    .setTransport(netHttpTransport)
                    .build()
                    .setAccessToken(response.getAccessToken())
                    .setRefreshToken(response.getRefreshToken());
        } catch (IOException e) {
            this.sendErrorResponse(chatId);
            throw new GmailException(e);
        }
    }

    public Gmail createGmailService(Credential credential) {
        return new Gmail.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName(googleProperties.getApplicationName())
                .build();
    }

    public void sendErrorResponse(Long chatId) {
        eventPublisher.publishEvent(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(MessagesUtil.AUTHORIZATION_FAILED)
                        .replyMarkup(ButtonsUtil.getAuthorizeInlineKeyboard(this.getAuthorizationUrl(chatId)))
                        .build()
        );
    }

}
