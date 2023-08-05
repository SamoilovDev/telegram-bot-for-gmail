package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.services.gmail.model.MessagePart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Slf4j
@Service
public class EmailProcessingService {

    private static final List<String> REQUIRED_HEADER_NAMES = List.of(
            "Replay-To",
            "Subject",
            "Date",
            "From",
            "To"
    );

    public String prepareMessagePart(MessagePart messagePart) {
        String mimeType = messagePart.getMimeType();

        if (Objects.nonNull(mimeType) &&  mimeType.equals("multipart/alternative")) {
            StringBuilder preparedMessage = new StringBuilder("Email found:");

            messagePart.getHeaders()
                    .stream()
                    .filter(header -> REQUIRED_HEADER_NAMES.contains(header.getName()))
                    .forEach(header -> preparedMessage.append("\n\n")
                                .append(header.getName())
                                .append(": ")
                                .append(header.getValue())
                                .append("\n")
                    );

            messagePart.getParts()
                    .stream()
                    .filter(msgPart -> msgPart.getMimeType().equals("text/plain") && Objects.nonNull(msgPart.getBody()))
                    .forEach(msgPart -> {
                        try {
                            log.error(msgPart.toPrettyString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String decodedMessage = new String(
                                        Base64.getUrlDecoder().decode(msgPart.getBody().getData().getBytes(UTF_8)),
                                        UTF_8
                                );
                        preparedMessage.append("\nMessage: ").append(decodedMessage);
                    });

            return preparedMessage.toString();
        } else return EMPTY;
    }
}
