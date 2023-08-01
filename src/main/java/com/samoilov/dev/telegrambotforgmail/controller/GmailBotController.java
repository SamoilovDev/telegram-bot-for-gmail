package com.samoilov.dev.telegrambotforgmail.controller;

import com.samoilov.dev.telegrambotforgmail.component.TelegramInformationMapper;
import com.samoilov.dev.telegrambotforgmail.config.properties.TelegramProperties;
import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.service.GmailBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GmailBotController extends TelegramLongPollingBot {

    private final TelegramInformationMapper telegramInformationMapper;

    private final TelegramProperties telegramProperties;

    private final GmailBotService gmailBotService;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            UpdateInformationDto preparedUpdate = telegramInformationMapper.mapFullUpdateToInformationDto(update);
            SendMessage responseMessage = gmailBotService.getResponseMessage(preparedUpdate);

            super.executeAsync(responseMessage);

            log.info("Message '{}' was send to chat with id {}", responseMessage.getText(), responseMessage.getChatId());
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
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
