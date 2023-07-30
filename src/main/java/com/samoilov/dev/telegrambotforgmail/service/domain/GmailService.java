package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.client.auth.oauth2.Credential;
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
import com.samoilov.dev.telegrambotforgmail.config.GoogleConfiguration;
import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import com.samoilov.dev.telegrambotforgmail.dto.CredentialDto;
import com.samoilov.dev.telegrambotforgmail.dto.GmailDto;
import com.samoilov.dev.telegrambotforgmail.exception.GmailCreatingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final GoogleProperties googleProperties;

    private static final List<String> SCOPES = List.of(GmailScopes.MAIL_GOOGLE_COM);

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public GmailDto getGmail(String userId) {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            CredentialDto credentialDto = this.getCredentials(httpTransport, userId);
            Gmail gmail = new Gmail
                    .Builder(httpTransport, JSON_FACTORY, credentialDto.getCredential())
                    .setApplicationName(googleProperties.getApplicationName())
                    .build();

            return GmailDto
                    .builder()
                    .authorizationUrl(credentialDto.getAuthorizationUrl())
                    .gmail(gmail)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new GmailCreatingException(e);
        }
    }

    private CredentialDto getCredentials(NetHttpTransport httpTransport, String userId) {
        try (
                InputStream in = GoogleConfiguration.class.getResourceAsStream(googleProperties.getCredentialsPath());
                InputStreamReader inr = new InputStreamReader(Objects.requireNonNull(in))
        ) {
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, inr);
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                    .Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
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

            return this.getCredentialDto(flow, receiver, userId);
        } catch (IOException | NullPointerException e) {
            throw new GmailCreatingException(e);
        }
    }

    private CredentialDto getCredentialDto(
            GoogleAuthorizationCodeFlow flow,
            LocalServerReceiver receiver,
            String userId) throws IOException {
        String authUrl = flow
                .newAuthorizationUrl()
                .setRedirectUri(receiver.getRedirectUri())
                .build();
        Credential credential = flow.loadCredential(userId);

        return CredentialDto
                .builder()
                .authorizationUrl(authUrl)
                .credential(credential)
                .build();
    }

}
