package com.samoilov.dev.telegrambotforgmail.api.service.domain;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import com.samoilov.dev.telegrambotforgmail.api.exception.AuthorizationUrlCreatingException;
import com.samoilov.dev.telegrambotforgmail.api.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.api.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.api.service.util.MessagesUtil;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailConnectionService {

    private final GoogleClientSecrets googleClientSecrets;

    private final NetHttpTransport netHttpTransport;

    private final GoogleProperties googleProperties;

    private final ApplicationEventPublisher eventPublisher;

    private final JsonFactory jsonFactory;

    private final List<String> scopes;

    private File tokenStore;

    private static final String OAUTH_2_GOOGLEAPIS_COM_TOKEN = "https://oauth2.googleapis.com/token";

    public String getAuthorizationUrl(Long chatId) {
        try {
            return new GoogleAuthorizationCodeFlow
                    .Builder(netHttpTransport, jsonFactory, googleClientSecrets, scopes)
                    .setDataStoreFactory(new FileDataStoreFactory(tokenStore))
                    .setAccessType("offline")
                    .build()
                    .newAuthorizationUrl()
                    .setRedirectUri("http://localhost:8080/oauth2callback/".concat(String.valueOf(chatId)))
                    .build();
        } catch (IOException | NullPointerException e) {
            eventPublisher.publishEvent(
                    new SendMessage(String.valueOf(chatId), "Impossible to authorize now, please try again")
            );
            throw new AuthorizationUrlCreatingException(e);
        }
    }


    @SuppressWarnings("deprecation")
    Credential exchangeCode(String authCode, String redirectUri, Long chatId) {
        try {
            GoogleClientSecrets.Details details = googleClientSecrets.getDetails();
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                            netHttpTransport,
                            jsonFactory,
                            OAUTH_2_GOOGLEAPIS_COM_TOKEN,
                            details.getClientId(),
                            details.getClientSecret(),
                            authCode,
                            redirectUri
                    )
                    .execute();

            return new GoogleCredential
                    .Builder()
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

    Gmail createGmailService(Credential credential) {
        return new Gmail
                .Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName(googleProperties.getApplicationName())
                .build();
    }

    void sendErrorResponse(Long chatId) {
        eventPublisher.publishEvent(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(MessagesUtil.AUTHORIZATION_FAILED)
                        .replyMarkup(ButtonsUtil.getAuthorizeInlineKeyboard(this.getAuthorizationUrl(chatId)))
                        .build()
        );
    }

    @PostConstruct
    private void createTokenStore() throws IOException {
        tokenStore = new File(googleProperties.getTokensPath());

        if (!tokenStore.exists()) {
            Files.createDirectories(tokenStore.toPath());
        }
    }

    @PreDestroy
    private void deleteTokenStore() throws IOException {
        Files.deleteIfExists(tokenStore.toPath());
    }

}
