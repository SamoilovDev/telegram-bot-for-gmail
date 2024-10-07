package com.samoilov.dev.telegrambotforgmail.mapper;

import com.samoilov.dev.telegrambotforgmail.store.dto.EmailMessageDto;
import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.entity.EmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface InformationMapper {

    @Mappings({
            @Mapping(source = "id", target = "telegramId"),
            @Mapping(source = "lastName", target = "lastName"),
            @Mapping(source = "userName", target = "userName"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    })
    UserEntity mapTelegramUserToEntity(User user);

    @Mappings({
            @Mapping(source = "text", target = "message"),
            @Mapping(source = "from", target = "telegramUser")
    })
    UpdateInformationDto mapMessageToUpdateInformationDto(Message message);

    @Mappings({
            @Mapping(target = "chatId", expression = "java(callbackQuery.getMessage().getChatId())"),
            @Mapping(target = "message", source = "data"),
            @Mapping(target = "telegramUser", source = "from")
    })
    UpdateInformationDto mapCallbackQueryToUpdateInformationDto(CallbackQuery callbackQuery);

    @Mappings({
            @Mapping(target = "createDate", source = "createdAt"),
            @Mapping(target = "emails", qualifiedByName = "mapEmailEntitiesToStringList")
    })
    UserDto mapEntityToDto(UserEntity userEntity);

    @Named("mapEmailEntitiesToStringList")
    default List<String> mapEmailEntitiesToStringList(List<EmailEntity> emailEntities) {
        return emailEntities
                .stream()
                .map(EmailEntity::getEmail)
                .toList();
    }

    default MimeMessage mapEmailMessageDtoToMime(EmailMessageDto emailMessageDto) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(emailMessageDto.getFrom()));
            mimeMessage.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(emailMessageDto.getTo()));
            mimeMessage.setSubject(emailMessageDto.getSubject());
            mimeMessage.setText(emailMessageDto.getBodyText());

            return mimeMessage;
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    default UpdateInformationDto mapFullUpdateToInformationDto(Update update) {
        return update.hasMessage()
                ? this.mapMessageToUpdateInformationDto(update.getMessage())
                : this.mapCallbackQueryToUpdateInformationDto(update.getCallbackQuery());
    }

}
