package com.samoilov.dev.telegrambotforgmail.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@Data
@ConfigurationProperties(prefix = "bot.telegram")
@PropertySource(value = "classpath:application.properties")
public class TelegramProperties {

    @Value("${bot.telegram.name}")
    private String name;

    @Value("${bot.telegram.token}")
    private String token;

}
