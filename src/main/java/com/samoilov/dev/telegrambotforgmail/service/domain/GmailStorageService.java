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

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GmailStorageService {

    private final RedisTemplate<Long, AuthenticationInfoDto> redisTemplate;

    private final ApplicationEventPublisher eventPublisher;

    @EventListener(AuthenticationInfoDto.class)
    public void handleAndSaveAuthInfoDto(AuthenticationInfoDto authenticationInfoDto) {
        if (Objects.nonNull(this.getAuthInfoByChatId(authenticationInfoDto.getChatId()))) {
            redisTemplate.delete(List.of(authenticationInfoDto.getChatId()));
        }

        redisTemplate
                .opsForValue()
                .set(authenticationInfoDto.getChatId(), authenticationInfoDto);

        eventPublisher.publishEvent(
                SendMessage
                        .builder()
                        .chatId(authenticationInfoDto.getChatId())
                        .text(MessagesUtil.SUCCESS_AUTHORIZATION)
                        .replyMarkup(ButtonsUtil.getReplyKeyboard(true))
                        .build()
        );
    }

    public AuthenticationInfoDto getAuthInfoByChatId(Long chatId) {
        return redisTemplate
                .opsForValue()
                .get(chatId);
    }

}
