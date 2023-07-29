package com.samoilov.dev.telegrambotforgmail.controller;

import com.samoilov.dev.telegrambotforgmail.config.properties.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Controller
@RequiredArgsConstructor
public class GmailBotController extends TelegramLongPollingBot {

    private final TelegramProperties telegramProperties;


    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return telegramProperties.getName();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getBotToken() {
        return telegramProperties.getToken();
    }

}
