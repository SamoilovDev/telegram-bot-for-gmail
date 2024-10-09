package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;

public interface GmailCacheService {

    AuthenticationInfoDto getAuthenticationInfoByChatId(Long chatId);

    Gmail getGmail(Long chatId);

}
