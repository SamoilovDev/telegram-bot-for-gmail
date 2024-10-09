package com.samoilov.dev.telegrambotforgmail.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.service.GmailAuthorizationService;
import com.samoilov.dev.telegrambotforgmail.service.GmailCacheService;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static com.samoilov.dev.telegrambotforgmail.config.CacheConfiguration.AUTHENTICATION_INFO_CACHE_NAME;
import static com.samoilov.dev.telegrambotforgmail.config.CacheConfiguration.CACHE_MANAGER_BEAN_NAME;
import static com.samoilov.dev.telegrambotforgmail.config.CacheConfiguration.GMAIL_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class GmailCacheServiceImpl implements GmailCacheService {

    private final GmailAuthorizationService gmailAuthorizationService;
    private final ApplicationContext applicationContext;
    private final CacheManager cacheManager;

    @Override
    @Cacheable(
            cacheNames = AUTHENTICATION_INFO_CACHE_NAME,
            cacheManager = CACHE_MANAGER_BEAN_NAME,
            key = "#chatId")
    public AuthenticationInfoDto getAuthenticationInfoByChatId(Long chatId) {
        AuthenticationInfoDto authenticationInfo = Optional
                .ofNullable(cacheManager.getCache(AUTHENTICATION_INFO_CACHE_NAME))
                .map(authCache -> authCache.get(chatId, AuthenticationInfoDto.class))
                .orElse(null);

        if (Objects.isNull(authenticationInfo)) {
            gmailAuthorizationService.sendAuthorizationFailedMessage(chatId);
            throw new GmailException();
        }

        return authenticationInfo;
    }

    @Override
    @Cacheable(
            cacheNames = GMAIL_CACHE_NAME,
            cacheManager = CACHE_MANAGER_BEAN_NAME,
            key = "#chatId")
    public Gmail getGmail(Long chatId) {
        AuthenticationInfoDto usersAuthInfo = this.getSelf().getAuthenticationInfoByChatId(chatId);
        Credential credentials = gmailAuthorizationService.exchangeAuthorizationCode(
                usersAuthInfo.getAuthCode(), usersAuthInfo.getRedirectUri(), chatId
        );

        return gmailAuthorizationService.authorizeGmail(credentials);
    }

    private GmailCacheService getSelf() {
        return applicationContext.getBean(GmailCacheService.class);
    }

}
