package com.samoilov.dev.telegrambotforgmail.component;

import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Slf4j
@Component
public class InformationMapper {

    public UserEntity mapTelegramUserToEntity(User user) {
        return UserEntity.builder()
                .telegramId(user.getId())
                .firstName(user.getFirstName())
                .lastName(Objects.isNull(user.getLastName()) ? EMPTY : user.getLastName())
                .userName(Objects.isNull(user.getUserName()) ? EMPTY : user.getUserName())
                .build();
    }

    public UserDto mapEntityToDto(UserEntity userEntity) {
        return UserDto.builder()
                .telegramId(userEntity.getTelegramId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .userName(userEntity.getUserName())
                .email(userEntity.getEmail())
                .build();
    }

    public UpdateInformationDto mapFullUpdateToInformationDto(Update update) {
        if (update.hasMessage()) {
            return UpdateInformationDto.builder()
                    .chatId(update.getMessage().getChatId())
                    .message(update.getMessage().getText())
                    .user(update.getMessage().getFrom())
                    .build();
        } else {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return UpdateInformationDto.builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .message(callbackQuery.getData())
                    .user(callbackQuery.getFrom())
                    .build();
        }
    }

}
