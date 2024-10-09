package com.samoilov.dev.telegrambotforgmail.service.impl;

import com.samoilov.dev.telegrambotforgmail.exception.UserNotFoundException;
import com.samoilov.dev.telegrambotforgmail.mapper.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.service.UserService;
import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.entity.EmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import com.samoilov.dev.telegrambotforgmail.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final InformationMapper informationMapper;
    private final UserRepository userRepository;

    @Override
    public UserDto saveUser(User user) {
        if (userRepository.existsByTelegramId(user.getId())) {
            UserEntity foundedUser = this.getUserEntityByTelegramId(user.getId());

            if (foundedUser.getCommandCounter() % 10 == 0) {
                return informationMapper.mapEntityToDto(foundedUser);
            }
        }

        return this.saveChangedUserData(user);
    }

    @Override
    public void addEmailAddress(Long telegramId, String email) {
        UserEntity foundedUser = this.getUserEntityByTelegramId(telegramId);
        List<EmailEntity> emails = foundedUser.getEmails();

        emails.add(EmailEntity.builder().email(email).build());
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


    private UserDto saveChangedUserData(User userToSave) {
        UserEntity mappedUser = informationMapper.mapTelegramUserToEntity(userToSave);

        return informationMapper.mapEntityToDto(userRepository.save(mappedUser));
    }

    private UserEntity getUserEntityByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

}
