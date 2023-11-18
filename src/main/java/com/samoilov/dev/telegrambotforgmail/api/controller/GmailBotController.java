package com.samoilov.dev.telegrambotforgmail.api.controller;

import com.samoilov.dev.telegrambotforgmail.api.service.GmailBotService;
import com.samoilov.dev.telegrambotforgmail.api.service.mapper.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.config.properties.TelegramProperties;
import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GmailBotController extends TelegramLongPollingBot {

    private final InformationMapper informationMapper;

    private final TelegramProperties telegramProperties;

    private final GmailBotService gmailBotService;

    @EventListener(SendMessage.class)
    public void sendMessage(SendMessage responseMessage) {
        try {
            super.executeAsync(responseMessage);
            log.info(
                    "Message \"{}\" was send to chat with id {}",
                    responseMessage.getText(),
                    responseMessage.getChatId()
            );
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        UpdateInformationDto preparedUpdate = informationMapper.mapFullUpdateToInformationDto(update);
        SendMessage responseMessage = gmailBotService.getResponseMessage(preparedUpdate);
        this.sendMessage(responseMessage);
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
