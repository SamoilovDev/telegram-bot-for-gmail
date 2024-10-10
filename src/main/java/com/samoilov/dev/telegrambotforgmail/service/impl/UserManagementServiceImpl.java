package com.samoilov.dev.telegrambotforgmail.service.impl;

import com.samoilov.dev.telegrambotforgmail.exception.UserNotFoundException;
import com.samoilov.dev.telegrambotforgmail.mapper.UserInfoMapper;
import com.samoilov.dev.telegrambotforgmail.service.UserManagementService;
import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.entity.GmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import com.samoilov.dev.telegrambotforgmail.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserInfoMapper userInfoMapper;
    private final UserRepository userRepository;

    @Override
    public UserDto saveUser(User user) {
        UserEntity foundedUser = Optional.of(user)
                .filter(u -> userRepository.existsByTelegramId(u.getId()))
                .map(u -> this.getUserEntityByTelegramId(u.getId()))
                .orElseGet(() -> userRepository.save(
                        userInfoMapper.mapTelegramUserToEntity(user)
                ));

        if (foundedUser.getCommandCounter() % 10 == 0) {
            userRepository.save(foundedUser);
        }

        return userInfoMapper.mapEntityToDto(foundedUser);
    }

    @Override
    public void addEmailAddress(Long telegramId, String email) {
        UserEntity foundedUser = this.getUserEntityByTelegramId(telegramId);
        List<GmailEntity> emails = foundedUser.getEmails();

        emails.add(GmailEntity.builder().emailAddress(email).build());
        foundedUser.setEmails(emails);
        userRepository.save(foundedUser);
    }

    @Override
    public void addChatId(Long telegramId, Long chatId) {
        UserEntity foundedUser = this.getUserEntityByTelegramId(telegramId);

        foundedUser.getChatIds().add(chatId);
        userRepository.save(foundedUser);
    }

    @Override
    public void incrementCommandCounter(Long telegramId) {
        userRepository.incrementCount(telegramId);
    }

    @Override
    public void disableUser(Long telegramId) {
        userRepository.disableUser(telegramId);
    }

    @Override
    public List<Long> getAllChatIds() {
        return userRepository.findAll()
                .stream()
                .map(UserEntity::getChatIds)
                .flatMap(List::stream)
                .toList();
    }

    private UserEntity getUserEntityByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

}
