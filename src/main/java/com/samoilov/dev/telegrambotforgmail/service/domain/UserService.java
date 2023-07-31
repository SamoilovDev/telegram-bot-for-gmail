package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.samoilov.dev.telegrambotforgmail.component.UserCredentialsMapper;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
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

    public UserDto saveUser(User user) {
        if (!userRepository.existsByTelegramId(user.getId())) {
            return this.saveChangedUserData(user);
        }

        UserEntity foundedUser = this.getUserEntityByTelegramId(user.getId());

        return foundedUser.getCommandCounter() % 10 == 0
                ? this.saveChangedUserData(user)
                : userCredentialsMapper.mapEntityToDto(foundedUser);
    }

    public UserDto getUserByTelegramId(Long telegramId) {
        return userCredentialsMapper.mapEntityToDto(
                this.getUserEntityByTelegramId(telegramId)
        );
    }

    public void incrementCommandCounter(Long telegramId) {
        userRepository.incrementCount(telegramId);
    }

    public void disableUser(Long telegramId) {
        userRepository.disableUser(telegramId);
    }

    private UserDto saveChangedUserData(User userToSave) {
        UserEntity mappedUser = userCredentialsMapper.mapTelegramUserToEntity(userToSave);

        return userCredentialsMapper.mapEntityToDto(
                userRepository.save(mappedUser)
        );
    }

    private UserEntity getUserEntityByTelegramId(Long telegramId) {
        return userRepository
                .findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

}
