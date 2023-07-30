package com.samoilov.dev.telegrambotforgmail.dto;

import com.google.api.services.gmail.Gmail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GmailDto {

    private String authorizationUrl;

    private Gmail gmail;

}
