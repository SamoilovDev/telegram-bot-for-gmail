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
public class EmailMessageDto implements Serializable {

    private String bodyText;
    private String subject;
    private String from;
    private String to;

}
