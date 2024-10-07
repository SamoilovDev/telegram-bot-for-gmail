package com.samoilov.dev.telegrambotforgmail.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInformationDto {

    private String message;

    private Long chatId;

    private User telegramUser;

}
