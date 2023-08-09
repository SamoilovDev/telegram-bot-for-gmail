package com.samoilov.dev.telegrambotforgmail.component;

import com.samoilov.dev.telegrambotforgmail.dto.EmailMessageDto;
import com.samoilov.dev.telegrambotforgmail.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.entity.EmailEntity;
import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Properties;

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
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public MimeMessage mapEmailMessageDtoToMime(EmailMessageDto emailMessageDto) throws MessagingException {
        Session session = Session.getDefaultInstance(new Properties(), null);
        MimeMessage mimeMessage = new MimeMessage(session);

        mimeMessage.setFrom(new InternetAddress(emailMessageDto.getFrom()));
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailMessageDto.getTo()));
        mimeMessage.setSubject(emailMessageDto.getSubject());
        mimeMessage.setText(emailMessageDto.getBodyText());

        return mimeMessage;
    }

    public UserDto mapEntityToDto(UserEntity userEntity) {
        return UserDto.builder()
                .telegramId(userEntity.getTelegramId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .userName(userEntity.getUserName())
                .emails(
                        userEntity.getEmails()
                                .stream()
                                .map(this::mapEmailEntityToString)
                                .toList()
                )
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

    private String mapEmailEntityToString(EmailEntity emailEntity) {
        return emailEntity.getEmail();
    }

}
