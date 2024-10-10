package com.samoilov.dev.telegrambotforgmail.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UpdateInformationDto implements Serializable {

    private User telegramUser;
    private String message;
    private Long chatId;

}
