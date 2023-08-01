package com.samoilov.dev.telegrambotforgmail.controller;

import com.samoilov.dev.telegrambotforgmail.dto.AuthenticationInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuth2CallbackController {

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/oauth2callback/{chatId}")
    public void handleOAuth2Callback(
            @RequestParam(name = "code") String authorizationCode,
            @PathVariable(name = "chatId") Long chatId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            eventPublisher.publishEvent(
                    AuthenticationInfoDto
                            .builder()
                            .authCode(authorizationCode)
                            .redirectUri(request.getRequestURL().toString())
                            .chatId(chatId)
                            .build()
            );

            response.sendRedirect("https://t.me/GmailCheckerBot");
        } catch (IOException e) {
            log.error("Error while handling OAuth2 callback", e);
        }
    }

}
