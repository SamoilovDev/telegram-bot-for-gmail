package com.samoilov.dev.telegrambotforgmail.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@Data
@ConfigurationProperties(prefix = "google.gmail")
@PropertySource(value = "classpath:application.properties")
public class GoogleProperties {

    @Value("${google.gmail.application-name}")
    private String applicationName;

    @Value("${google.gmail.tokens-directory-path}")
    private String tokensPath;

    @Value("${google.gmail.credentials-path}")
    private String credentialsPath;

}
