package com.samoilov.dev.telegrambotforgmail.dto;

import com.google.api.client.auth.oauth2.Credential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialDto {

    private String authorizationUrl;

    private Credential credential;

}
