package com.samoilov.dev.telegrambotforgmail.service.impl;

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
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.service.GmailAuthorizationService;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import com.samoilov.dev.telegrambotforgmail.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.util.MessagesUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static com.samoilov.dev.telegrambotforgmail.config.CacheConfiguration.AUTHENTICATION_INFO_CACHE_NAME;
import static com.samoilov.dev.telegrambotforgmail.util.MessagesUtil.IMPOSSIBLE_TO_AUTHORIZE_NOW;

@Service
@RequiredArgsConstructor
public class GmailAuthorizationServiceImpl implements GmailAuthorizationService {

    private static final String GOOGLE_ACCESS_TYPE = "offline";
    
    private final ApplicationEventPublisher eventPublisher;
    private final GoogleClientSecrets googleClientSecrets;
    private final GoogleProperties googleProperties;
    private final NetHttpTransport netHttpTransport;
    private final CacheManager cacheManager;
    private final JsonFactory jsonFactory;

    private File tokenStore;

    @Override
    public void handleAuthenticationInfo(AuthenticationInfoDto authenticationInfoDto) {
        Objects.requireNonNull(cacheManager.getCache(AUTHENTICATION_INFO_CACHE_NAME))
                .put(authenticationInfoDto.getChatId(), authenticationInfoDto);

        eventPublisher.publishEvent(
                SendMessage.builder()
                        .chatId(authenticationInfoDto.getChatId())
                        .text(MessagesUtil.SUCCESS_AUTHORIZATION)
                        .replyMarkup(ButtonsUtil.getGmailStartKeyboard())
                        .build()
        );
    }

    @Override
    public String getAuthorizationUrl(Long chatId) {
        try {
            return new GoogleAuthorizationCodeFlow
                    .Builder(netHttpTransport, jsonFactory, googleClientSecrets, googleProperties.getScopes())
                    .setDataStoreFactory(new FileDataStoreFactory(tokenStore))
                    .setAccessType(GOOGLE_ACCESS_TYPE)
                    .build()
                    .newAuthorizationUrl()
                    .setRedirectUri(googleProperties.getRedirectUri().concat(String.valueOf(chatId)))
                    .build();
        } catch (IOException | NullPointerException e) {
            eventPublisher.publishEvent(new SendMessage(String.valueOf(chatId), IMPOSSIBLE_TO_AUTHORIZE_NOW));
            throw new AuthorizationUrlCreatingException(e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public Credential exchangeAuthorizationCode(String authCode, String redirectUri, Long chatId) {
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
            this.sendAuthorizationFailedMessage(chatId);
            throw new GmailException(e);
        }
    }

    @Override
    public Gmail authorizeGmail(Credential credential) {
        return new Gmail.Builder(netHttpTransport, jsonFactory, credential)
                .setApplicationName(googleProperties.getApplicationName())
                .build();
    }

    @Override
    public void sendAuthorizationFailedMessage(Long chatId) {
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
