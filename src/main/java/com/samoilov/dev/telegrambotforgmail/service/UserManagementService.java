package com.samoilov.dev.telegrambotforgmail.service;

import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

public interface UserManagementService {

    @Transactional
    UserDto saveUser(User user);

    @Transactional
    void addEmailAddressIfNeeded(Long telegramId, String email);

    @Transactional
    void addChatId(Long telegramId, Long chatId);

    @Transactional
    void incrementCommandCounter(Long telegramId);

    @Transactional
    void disableUser(Long telegramId);

    @Transactional
    List<Long> getAllChatIds();

}
