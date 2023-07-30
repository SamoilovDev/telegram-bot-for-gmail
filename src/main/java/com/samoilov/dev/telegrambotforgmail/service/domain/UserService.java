package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.samoilov.dev.telegrambotforgmail.component.UserCredentialsMapper;
import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import com.samoilov.dev.telegrambotforgmail.exception.UserNotFoundException;
import com.samoilov.dev.telegrambotforgmail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserCredentialsMapper userCredentialsMapper;

    private final UserRepository userRepository;

    public UserEntity saveNewUser(User user) {
        if (!userRepository.existsByTelegramId(user.getId())) {
            UserEntity userEntity = userCredentialsMapper.mapTelegramUserToEntity(user);
            return userRepository.save(userEntity);
        } else {
            return this.getUserByTelegramId(user.getId());
        }
    }

    public UserEntity getUserByTelegramId(Long telegramId) {
        return userRepository
                .findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

    public void incrementCommandCounter(Long telegramId) {
        userRepository.incrementCount(telegramId);
    }

}
