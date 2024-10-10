package com.samoilov.dev.telegrambotforgmail.controller;

import com.samoilov.dev.telegrambotforgmail.controller.api.OAuth2CallbackApi;
import com.samoilov.dev.telegrambotforgmail.store.dto.AuthenticationInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuth2CallbackController implements OAuth2CallbackApi {

    private final ApplicationEventPublisher eventPublisher;

    private static final String REDIRECT_URL = "https://t.me/GmailCheckerBot";

    @Override
    public void handleOAuth2Callback(
            String authorizationCode,
            Long chatId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            eventPublisher.publishEvent(AuthenticationInfoDto.builder()
                    .authCode(authorizationCode)
                    .redirectUri(request.getRequestURL().toString())
                    .chatId(chatId)
                    .build());

            response.sendRedirect(REDIRECT_URL);
        } catch (IOException e) {
            log.error("Error during OAuth2 callback handling", e);
        }
    }

}
