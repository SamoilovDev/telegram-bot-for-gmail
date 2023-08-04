package com.samoilov.dev.telegrambotforgmail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationInfoDto {

    private Long chatId;

    private String authCode;

    private String redirectUri;

}
