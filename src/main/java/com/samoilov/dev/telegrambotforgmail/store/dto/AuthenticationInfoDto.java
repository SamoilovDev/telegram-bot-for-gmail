package com.samoilov.dev.telegrambotforgmail.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthenticationInfoDto implements Serializable {

    private String redirectUri;
    private String authCode;
    private Long chatId;

}
