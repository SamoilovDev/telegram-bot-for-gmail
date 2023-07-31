package com.samoilov.dev.telegrambotforgmail.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.dto.GmailDto;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
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

    private final GmailService gmailService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/oauth2callback/{chatId}")
    public void handleOAuth2Callback(
            @RequestParam(name = "code") String authorizationCode,
            @PathVariable(name = "chatId") Long chatId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Credential credential = gmailService.exchangeCode(authorizationCode, request.getRequestURL().toString());
            Gmail gmail = gmailService.createGmailService(credential);

            eventPublisher.publishEvent(
                    GmailDto.builder().gmail(gmail).chatId(chatId).build()
            );

            response.sendRedirect("https://t.me/GmailCheckerBot");
        } catch (IOException e) {
            log.error("Error while handling OAuth2 callback", e);
        }
    }

}
