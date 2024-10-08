package com.samoilov.dev.telegrambotforgmail.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


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

    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    @Bean
    public GoogleClientSecrets googleClientSecrets(JsonFactory jsonFactory) throws IOException {
        try (InputStream in = GoogleConfiguration.class.getResourceAsStream(googleProperties.getCredentialsPath());
             InputStreamReader inr = new InputStreamReader(Objects.requireNonNull(in))) {
            return GoogleClientSecrets.load(jsonFactory, inr);
        }
    }

    @Bean("scopes")
    public List<String> scopes() {
        return List.of(GmailScopes.MAIL_GOOGLE_COM);
    }

    @Bean
    public NetHttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

}
