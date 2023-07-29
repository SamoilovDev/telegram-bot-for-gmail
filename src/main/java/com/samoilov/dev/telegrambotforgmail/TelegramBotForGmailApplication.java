package com.samoilov.dev.telegrambotforgmail;

import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import com.samoilov.dev.telegrambotforgmail.config.properties.TelegramProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        TelegramProperties.class,
        GoogleProperties.class
})
public class TelegramBotForGmailApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotForGmailApplication.class, args);
    }

}
