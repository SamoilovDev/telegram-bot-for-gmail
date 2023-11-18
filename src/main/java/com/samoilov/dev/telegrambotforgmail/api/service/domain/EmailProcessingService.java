package com.samoilov.dev.telegrambotforgmail.api.service.domain;

import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.samoilov.dev.telegrambotforgmail.api.service.mapper.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.store.dto.EmailMessageDto;
import com.samoilov.dev.telegrambotforgmail.api.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.api.service.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.api.service.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.EMAIL_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.HTML_TAG_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.HTML_WHITESPACES_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.LINK_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.NEW_LINE;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.NEXT_MAIL_POINT_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.NOTHING;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.PREPARED_LINK;
import static com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil.REDUNDANT_SPACES_REGEXP;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessingService {

    private final InformationMapper informationMapper;

    private final ApplicationEventPublisher eventPublisher;

    private static final List<String> REQUIRED_HEADER_NAMES = List.of(
            "From",
            "To",
            "Replay-To",
            "Date",
            "Subject"
    );


    public String prepareMessagePart(MessagePart messagePart) {
        StringBuilder preparedMessage = this.createPreparedHeadersPart(messagePart.getHeaders());
        Optional<String> bodyPart = Optional.empty();
        String mimeType = messagePart.getMimeType();

        if (Objects.nonNull(mimeType) && mimeType.equals("multipart/alternative")) {
            bodyPart = messagePart.getParts()
                    .stream()
                    .filter(this::filterBody)
                    .map(MessagePart::getBody)
                    .map(MessagePartBody::getData)
                    .findFirst();
        }

        return bodyPart.map(body -> this.createFullEmailMessage(preparedMessage, body))
                .orElse(EMPTY);
    }

    public MimeMessage prepareRawMessageToMime(String rawMessage, String fromEmail, Long chatId) {
        try {
            String[] splitRawMessage = rawMessage.split(NEXT_MAIL_POINT_REGEXP, 3);

            if (splitRawMessage.length < 3 || !splitRawMessage[0].matches(EMAIL_REGEXP)) {
                throw new MessagingException();
            }

            EmailMessageDto emailMessageDto = EmailMessageDto
                    .builder()
                    .from(fromEmail)
                    .to(splitRawMessage[0])
                    .subject(splitRawMessage[1].equals(NOTHING) ? null : splitRawMessage[1])
                    .bodyText(splitRawMessage[2].equals(NOTHING) ? null : splitRawMessage[2])
                    .build();

            return informationMapper.mapEmailMessageDtoToMime(emailMessageDto);
        } catch (MessagingException e) {
            eventPublisher.publishEvent(
                    SendMessage
                            .builder()
                            .chatId(chatId)
                            .text(MessagesUtil.SEND_ERROR)
                            .replyMarkup(ButtonsUtil.getGmailSendMessageTemplateKeyboard())
                            .parseMode(ParseMode.MARKDOWN)
                            .build()
            );
            throw new GmailException(e);
        }
    }

    private StringBuilder createPreparedHeadersPart(List<MessagePartHeader> headers) {
        StringBuilder preparedMessage = new StringBuilder("Email found:\n");
        Map<String, String> headersMap = new HashMap<>();

        headers.stream()
                .filter(header -> REQUIRED_HEADER_NAMES.contains(header.getName()))
                .forEach(header -> {
                    String headerName = header.getName();
                    String value = header.getValue();
                    headersMap.put(
                            header.getName(),
                            headerName.equals("Date")
                                    ? value.substring(0, value.length() - 6)
                                    : value
                    );
                });

        REQUIRED_HEADER_NAMES.stream()
                .filter(headersMap::containsKey)
                .forEachOrdered(headerName -> preparedMessage.append(NEW_LINE)
                        .append(headerName)
                        .append(": ")
                        .append(headersMap.get(headerName)));

        return preparedMessage;
    }

    private boolean filterBody(MessagePart messagePart) {
        MessagePartBody body = messagePart.getBody();
        String mimeTypePart = messagePart.getMimeType();

        return (mimeTypePart.equals("text/plain") || mimeTypePart.equals("text/html"))
                && Objects.nonNull(body)
                && !body.getData().isBlank();
    }

    private String createFullEmailMessage(StringBuilder preparedHeaders, String body) {
        String decodedMessage = new String(Base64.getUrlDecoder().decode(body.getBytes(UTF_8)), UTF_8);
        String abbreviatedMessage = decodedMessage
                .replaceAll(LINK_REGEXP, PREPARED_LINK)
                .replaceAll(HTML_TAG_REGEXP, EMPTY)
                .replaceAll(HTML_WHITESPACES_REGEXP, NEW_LINE)
                .replaceAll(REDUNDANT_SPACES_REGEXP, NEW_LINE);

        preparedHeaders.append("\n\nMessage:\n").append(abbreviatedMessage);

        return preparedHeaders.toString();
    }

}
