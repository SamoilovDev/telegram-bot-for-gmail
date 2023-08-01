package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.samoilov.dev.telegrambotforgmail.dto.AuthenticationInfoDto;
import com.samoilov.dev.telegrambotforgmail.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class GmailStorageService {

    private final RedisTemplate<Long, AuthenticationInfoDto> redisTemplate;

    private final ApplicationEventPublisher eventPublisher;

    @EventListener(AuthenticationInfoDto.class)
    public void handleAndSaveAuthInfoDto(AuthenticationInfoDto authenticationInfoDto) {
        redisTemplate
                .opsForValue()
                .set(authenticationInfoDto.getChatId(), authenticationInfoDto);

        eventPublisher.publishEvent(
                SendMessage
                        .builder()
                        .chatId(authenticationInfoDto.getChatId())
                        .text(MessagesUtil.SUCCESS_AUTHORIZATION)
                        .replyMarkup(ButtonsUtil.getGmailKeyboard())
                        .build()
        );
    }

    public AuthenticationInfoDto getAuthInfoByChatId(Long chatId) {
        return redisTemplate.opsForValue().get(chatId);
    }

}
