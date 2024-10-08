package com.samoilov.dev.telegrambotforgmail.service;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import org.springframework.context.event.EventListener;

public interface GmailCacheService {

    @EventListener(AuthenticationInfoDto.class)
    void handleAuthenticationInfo(AuthenticationInfoDto authenticationInfoDto);

    AuthenticationInfoDto getAuthInfoByChatId(Long chatId);

    Gmail getGmail(Long chatId);
}
