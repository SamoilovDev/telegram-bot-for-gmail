package com.samoilov.dev.telegrambotforgmail.service;

import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface GmailBotService {

    SendMessage getResponseMessage(UpdateInformationDto preparedUpdate);

}
