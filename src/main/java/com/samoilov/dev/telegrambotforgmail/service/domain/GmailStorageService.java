package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.samoilov.dev.telegrambotforgmail.dto.AuthenticationInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailStorageService {

    private final RedisTemplate<Long, AuthenticationInfoDto> redisTemplate;

    @EventListener(AuthenticationInfoDto.class)
    public void handleGmailDto(AuthenticationInfoDto authenticationInfoDto) {
        redisTemplate
                .opsForValue()
                .set(authenticationInfoDto.getChatId(), authenticationInfoDto);
    }

    public AuthenticationInfoDto getGmailByChatId(Long chatId) {
        return redisTemplate.opsForValue().get(chatId);
    }

}
