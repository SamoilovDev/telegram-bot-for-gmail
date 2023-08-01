package com.samoilov.dev.telegrambotforgmail.component;

import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Component
public class TelegramInformationMapper {

    public UserEntity mapTelegramUserToEntity(User user) {
        return UserEntity
                .builder()
                .telegramId(user.getId())
                .firstName(user.getFirstName())
                .lastName(Objects.isNull(user.getLastName()) ? EMPTY : user.getLastName())
                .userName(Objects.isNull(user.getUserName()) ? EMPTY : user.getUserName())
                .build();
    }

    public UserDto mapEntityToDto(UserEntity userEntity) {
        return UserDto
                .builder()
                .telegramId(userEntity.getTelegramId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .userName(userEntity.getUserName())
                .email(userEntity.getEmail())
                .build();
    }

    public UpdateInformationDto mapFullUpdateToInformationDto(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            return UpdateInformationDto
                    .builder()
                    .chatId(message.getChatId())
                    .message(message.getText())
                    .user(message.getFrom())
                    .build();
        } else {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return UpdateInformationDto
                    .builder()
                    .chatId(callbackQuery.getMessage().getChatId())
                    .message(callbackQuery.getData())
                    .user(callbackQuery.getFrom())
                    .build();
        }
    }

}
