package com.samoilov.dev.telegrambotforgmail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private List<String> emails;

}
