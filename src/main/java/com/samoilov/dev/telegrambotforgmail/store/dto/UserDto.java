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

    private LocalDateTime createDate;
    private ActiveType activeType;
    private Long commandCounter;
    private List<String> emails;
    private String firstName;
    private Long telegramId;
    private String lastName;
    private String userName;

}
