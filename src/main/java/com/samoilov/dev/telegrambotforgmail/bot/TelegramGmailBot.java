package com.samoilov.dev.telegrambotforgmail.bot;

import com.samoilov.dev.telegrambotforgmail.config.properties.TelegramBotProperties;
import com.samoilov.dev.telegrambotforgmail.mapper.TelegramInfoMapper;
import com.samoilov.dev.telegrambotforgmail.service.GmailBotService;
import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramGmailBot extends TelegramLongPollingBot {

    private final TelegramBotProperties telegramBotProperties;
    private final TelegramInfoMapper telegramInfoMapper;
    private final GmailBotService gmailBotService;

    @Override
    public void onUpdateReceived(Update update) {
        UpdateInformationDto preparedUpdate = telegramInfoMapper.mapFullUpdateToInformationDto(update);
        SendMessage responseMessage = gmailBotService.getResponseMessage(preparedUpdate);

        this.sendMessage(responseMessage);
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.getName();
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getBotToken() {
        return telegramBotProperties.getToken();
    }

    @EventListener(SendMessage.class)
    public void sendMessage(SendMessage responseMessage) {
        try {
            super.executeAsync(responseMessage);
            log.info("Message \"{}\" was send to chat with id {}", responseMessage.getText(), responseMessage.getChatId());
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

}
