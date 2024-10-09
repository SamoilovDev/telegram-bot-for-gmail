package com.samoilov.dev.telegrambotforgmail.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "google.gmail")
public class GoogleProperties {

    private String applicationName;
    private String credentialsPath;
    private List<String> scopes;
    private String redirectUri;
    private String tokensPath;
    private String oauthUrl;

}
