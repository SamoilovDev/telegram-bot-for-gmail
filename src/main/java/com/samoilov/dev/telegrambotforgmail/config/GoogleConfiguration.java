package com.samoilov.dev.telegrambotforgmail.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import com.samoilov.dev.telegrambotforgmail.exception.CredentialCreatingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GoogleConfiguration {

    private final GoogleProperties googleProperties;

    private final List<String> scopes = List.of(GmailScopes.MAIL_GOOGLE_COM);

    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public NetHttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public Credential credential(NetHttpTransport httpTransport, JsonFactory jsonFactory) {
        try (InputStream in = GoogleConfiguration.class.getResourceAsStream(googleProperties.getCredentialsPath())) {
            if (Objects.isNull(in)) {
                throw new FileNotFoundException("Resource not found: " + googleProperties.getCredentialsPath());
            }

            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                    .Builder(httpTransport, jsonFactory, clientSecrets, scopes)
                    .setDataStoreFactory(
                            new FileDataStoreFactory(
                                    new File(googleProperties.getTokensPath())
                            )
                    )
                    .setAccessType("offline")
                    .build();
            LocalServerReceiver receiver = new LocalServerReceiver
                    .Builder()
                    .setPort(8080)
                    .build();

            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        } catch (IOException e) {
            throw new CredentialCreatingException(e);
        }
    }

    @Bean
    public Gmail gmail(NetHttpTransport httpTransport, JsonFactory jsonFactory, Credential credential) {
        return new Gmail.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(googleProperties.getApplicationName())
                .build();
    }

}
