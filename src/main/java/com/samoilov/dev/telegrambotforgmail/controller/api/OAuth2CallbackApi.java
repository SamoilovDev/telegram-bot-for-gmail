package com.samoilov.dev.telegrambotforgmail.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface OAuth2CallbackApi {

    String OAUTH2_CALLBACK_URI = "/oauth2callback/{chatId}";

    @GetMapping(OAUTH2_CALLBACK_URI)
    void handleOAuth2Callback(
            @RequestParam("code") String authorizationCode,
            @PathVariable("chatId") Long chatId,
            HttpServletRequest request,
            HttpServletResponse response);

}
