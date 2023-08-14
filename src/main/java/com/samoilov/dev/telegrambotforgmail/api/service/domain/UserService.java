package com.samoilov.dev.telegrambotforgmail.api.service.domain;

import com.samoilov.dev.telegrambotforgmail.api.exception.UserNotFoundException;
import com.samoilov.dev.telegrambotforgmail.api.service.mapper.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.entity.EmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import com.samoilov.dev.telegrambotforgmail.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final InformationMapper informationMapper;

    private final UserRepository userRepository;

    public List<Long> getAllChatIds() {
        return userRepository
                .findAll()
                .stream()
                .map(UserEntity::getChatIds)
                .flatMap(List::stream)
                .toList();
    }

    private UserEntity getUserEntityByTelegramId(Long telegramId) {
        return userRepository
                .findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public UserDto saveUser(User user) {
        if (userRepository.existsByTelegramId(user.getId())) {
            UserEntity foundedUser = this.getUserEntityByTelegramId(user.getId());

            return foundedUser.getCommandCounter() % 10 == 0
                    ? this.saveChangedUserData(user)
                    : informationMapper.mapEntityToDto(foundedUser);
        }

        return this.saveChangedUserData(user);
    }

    @Transactional
    public void addEmailAddress(Long telegramId, String email) {
        UserEntity foundedUser = this.getUserEntityByTelegramId(telegramId);
        List<EmailEntity> emails = foundedUser.getEmails();

        emails.add(EmailEntity.builder().email(email).build());
        foundedUser.setEmails(emails);
        userRepository.save(foundedUser);
    }

    @Transactional
    public void addChatId(Long telegramId, Long chatId) {
        UserEntity foundedUser = this.getUserEntityByTelegramId(telegramId);

        foundedUser.getChatIds().add(chatId);
        userRepository.save(foundedUser);
    }

    @Transactional
    public void incrementCommandCounter(Long telegramId) {
        userRepository.incrementCount(telegramId);
    }

    @Transactional
    public void disableUser(Long telegramId) {
        userRepository.disableUser(telegramId);
    }


    private UserDto saveChangedUserData(User userToSave) {
        UserEntity mappedUser = informationMapper.mapTelegramUserToEntity(userToSave);

        return informationMapper.mapEntityToDto(
                userRepository.save(mappedUser)
        );
    }

}
