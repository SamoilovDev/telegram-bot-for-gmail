package com.samoilov.dev.telegrambotforgmail.dto;

import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessageDto {

    private List<MessagePartHeader> headers;

    private MessagePartBody message;

    @Override
    public String toString() {
        StringBuilder preparedMessage = new StringBuilder("Message found:");

        headers.forEach(
                header -> preparedMessage
                        .append("\n")
                        .append(header.getName())
                        .append(": ")
                        .append(header.getValue())
        );
        preparedMessage.append("\nmessage: ").append(message.getData());

        return preparedMessage.toString();
    }
}
