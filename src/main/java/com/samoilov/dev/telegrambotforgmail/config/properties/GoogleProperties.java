package com.samoilov.dev.telegrambotforgmail.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@Data
@ConfigurationProperties(prefix = "google.gmail")
@PropertySource(value = "classpath:application.properties")
public class GoogleProperties {

    private String tokensDirectoryPath;

    private String applicationName;

    private String credentialsPath;

    private String redirectUri;

    private String oauthUrl;

}
