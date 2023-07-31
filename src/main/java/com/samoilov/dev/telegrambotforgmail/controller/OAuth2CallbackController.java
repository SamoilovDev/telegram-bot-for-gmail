package com.samoilov.dev.telegrambotforgmail.controller;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.dto.GmailDto;
import com.samoilov.dev.telegrambotforgmail.service.domain.GmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class OAuth2CallbackController {

    private final GmailService gmailService;

    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/oauth2callback/{chatId}")
    public ResponseEntity<Object> handleOAuth2Callback(
            @RequestParam(name = "code") String authorizationCode,
            @PathVariable(name = "chatId") Long chatId,
            HttpServletRequest request) {
        URI redirectUri = URI.create("https://t.me/GmailCheckerBot");
        Credential credential = gmailService.exchangeCode(authorizationCode, request.getRequestURL().toString());
        Gmail gmail = gmailService.createGmailService(credential);

        eventPublisher.publishEvent(
                GmailDto.builder().gmail(gmail).chatId(chatId).build()
        );

        return ResponseEntity.created(redirectUri).build();
    }

}
