package com.samoilov.dev.telegrambotforgmail.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.gmail")
public class GoogleProperties {

    private String tokensDirectoryPath;

    private String applicationName;

    private String credentialsPath;

    private String redirectUri;

    private String oauthUrl;

}
