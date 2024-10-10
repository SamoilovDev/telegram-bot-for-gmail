package com.samoilov.dev.telegrambotforgmail.mapper;

import com.samoilov.dev.telegrambotforgmail.store.dto.UserDto;
import com.samoilov.dev.telegrambotforgmail.store.entity.GmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface UserInfoMapper {

    @Mappings({
            @Mapping(source = "id", target = "telegramId"),
            @Mapping(source = "lastName", target = "lastName"),
            @Mapping(source = "userName", target = "username"),
            @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    })
    UserEntity mapTelegramUserToEntity(User user);

    @Mappings({
            @Mapping(target = "createDate", source = "createdAt"),
            @Mapping(target = "emails", qualifiedByName = "mapEmailEntitiesToStringList")
    })
    UserDto mapEntityToDto(UserEntity userEntity);

    @Named("mapEmailEntitiesToStringList")
    default List<String> mapEmailEntitiesToStringList(List<GmailEntity> emailEntities) {
        return emailEntities.stream()
                .map(GmailEntity::getEmailAddress)
                .toList();
    }


}
