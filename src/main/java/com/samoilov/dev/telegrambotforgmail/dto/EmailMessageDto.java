package com.samoilov.dev.telegrambotforgmail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {

    private String from;

    private String to;

    private String subject;

    private String bodyText;

}
