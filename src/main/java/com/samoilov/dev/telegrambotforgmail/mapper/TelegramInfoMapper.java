package com.samoilov.dev.telegrambotforgmail.mapper;

import com.samoilov.dev.telegrambotforgmail.store.dto.EmailMessageDto;
import com.samoilov.dev.telegrambotforgmail.store.dto.UpdateInformationDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Mapper(componentModel = "spring")
public interface TelegramInfoMapper {

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
