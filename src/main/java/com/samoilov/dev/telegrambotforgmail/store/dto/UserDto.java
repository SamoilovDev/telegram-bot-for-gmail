package com.samoilov.dev.telegrambotforgmail.store.dto;

import com.samoilov.dev.telegrambotforgmail.store.enums.ActiveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {

    private Long telegramId;

    private String firstName;

    private String lastName;

    private String userName;

    private LocalDateTime createDate;

    private Long commandCounter;

    private ActiveType activeType;

    private List<String> emails;

}
