package com.samoilov.dev.telegrambotforgmail.service.domain;

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
import com.samoilov.dev.telegrambotforgmail.exception.AuthorizationUrlCreatingException;
import com.samoilov.dev.telegrambotforgmail.exception.GmailCreatingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private final GoogleClientSecrets googleClientSecrets;

    private final NetHttpTransport netHttpTransport;

    private final GoogleProperties googleProperties;

    private final JsonFactory jsonFactory;

    private final List<String> scopes;

    public String getAuthorizationUrl(Long chatId) {
        try {
            return new GoogleAuthorizationCodeFlow
                    .Builder(netHttpTransport, jsonFactory, googleClientSecrets, scopes)
                    .setDataStoreFactory(
                            new FileDataStoreFactory(
                                    new File(googleProperties.getTokensPath())
                            )
                    )
                    .setAccessType("offline")
                    .build()
                    .newAuthorizationUrl()
                    .setRedirectUri("http://localhost:8080/oauth2callback/".concat(String.valueOf(chatId)))
                    .build();
        } catch (IOException | NullPointerException e) {
            throw new AuthorizationUrlCreatingException(e);
        }
    }

    public Gmail getGmail(String authCode, String redirectUri) {
        Credential credentials = this.exchangeCode(authCode, redirectUri);
        return this.createGmailService(credentials);
    }

    @SuppressWarnings("deprecation")
    private Credential exchangeCode(String authCode, String redirectUri) {
        try {
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    netHttpTransport,
                    jsonFactory,
                    "https://oauth2.googleapis.com/token",
                    googleClientSecrets.getDetails().getClientId(),
                    googleClientSecrets.getDetails().getClientSecret(),
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
            throw new GmailCreatingException(e);
        }
    }

    private Gmail createGmailService(Credential credential) {
        return new Gmail.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName(googleProperties.getApplicationName())
                .build();
    }

}
