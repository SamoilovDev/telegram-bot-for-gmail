package com.samoilov.dev.telegrambotforgmail.api.service.domain;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import com.samoilov.dev.telegrambotforgmail.api.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.api.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.api.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GmailCacheService {

    private final CacheManager cacheManager;

    private final ApplicationEventPublisher eventPublisher;

    private final GmailConnectionService gmailConnectionService;

    @EventListener(AuthenticationInfoDto.class)
    public void handleAndSaveAuthInfoDto(AuthenticationInfoDto authenticationInfoDto) {
        Objects.requireNonNull(cacheManager.getCache("authenticationInfo"))
                .put(authenticationInfoDto.getChatId(), authenticationInfoDto);

        eventPublisher.publishEvent(
                SendMessage
                        .builder()
                        .chatId(authenticationInfoDto.getChatId())
                        .text(MessagesUtil.SUCCESS_AUTHORIZATION)
                        .replyMarkup(ButtonsUtil.getGmailStartKeyboard())
                        .build()
        );
    }

    @Cacheable(cacheNames = "authenticationInfo", key = "#chatId")
    public AuthenticationInfoDto getAuthInfoByChatId(Long chatId) {
        AuthenticationInfoDto authenticationInfo = Optional.ofNullable(cacheManager.getCache("authenticationInfo"))
                .map(authCache -> authCache.get(chatId, AuthenticationInfoDto.class))
                .orElse(null);

        if (Objects.isNull(authenticationInfo)) {
            gmailConnectionService.sendErrorResponse(chatId);
            throw new GmailException();
        }

        return authenticationInfo;
    }

    @Cacheable(cacheNames = "gmail", key = "#chatId")
    public Gmail getGmail(Long chatId) {
        AuthenticationInfoDto usersAuthInfo = this.getAuthInfoByChatId(chatId);
        Credential credentials = gmailConnectionService.exchangeCode(
                usersAuthInfo.getAuthCode(), usersAuthInfo.getRedirectUri(), chatId
        );

        return gmailConnectionService.createGmailService(credentials);
    }

}
