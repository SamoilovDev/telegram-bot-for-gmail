package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.samoilov.dev.telegrambotforgmail.mapper.InformationMapper;
import com.samoilov.dev.telegrambotforgmail.store.dto.EmailMessageDto;
import com.samoilov.dev.telegrambotforgmail.exception.GmailException;
import com.samoilov.dev.telegrambotforgmail.util.ButtonsUtil;
import com.samoilov.dev.telegrambotforgmail.util.MessagesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.function.Function;

import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.EMAIL_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.HTML_TAG_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.HTML_WHITESPACES_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.LINK_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.NEW_LINE;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.NEXT_MAIL_POINT_REGEXP;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.NOTHING;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.PREPARED_LINK;
import static com.samoilov.dev.telegrambotforgmail.util.RegexpUtil.REDUNDANT_SPACES_REGEXP;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Service
@RequiredArgsConstructor
public class EmailProcessingService {

    private final ApplicationEventPublisher eventPublisher;
    private final InformationMapper informationMapper;

    private static final List<String> REQUIRED_HEADER_NAMES = List.of(
            "From",
            "To",
            "Replay-To",
            "Date",
            "Subject"
    );


    public String prepareMessagePart(MessagePart messagePart) {
        StringBuilder preparedMessage = this.createPreparedHeadersPart(messagePart.getHeaders());
        String mimeType = messagePart.getMimeType();

        if (Objects.isNull(mimeType) || !mimeType.equals("multipart/alternative")) {
            return EMPTY;
        }

        return messagePart.getParts()
                .stream()
                .filter(this::filterBody)
                .map(MessagePart::getBody)
                .map(MessagePartBody::getData)
                .findFirst()
                .map(body -> this.createFullEmailMessage(preparedMessage, body))
                .orElse(EMPTY);
    }

    public MimeMessage prepareRawMessageToMime(String rawMessage, String fromEmail, Long chatId) {
        String[] splitRawMessage = rawMessage.split(NEXT_MAIL_POINT_REGEXP, 3);
        Function<String, String> checkByEmptyFunc = rawPart -> Optional.of(rawPart)
                .filter(part -> !part.equals(NOTHING))
                .orElse(null);

        if (splitRawMessage.length < 3 || !splitRawMessage[0].matches(EMAIL_REGEXP)) {
            eventPublisher.publishEvent(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(MessagesUtil.SEND_ERROR)
                            .replyMarkup(ButtonsUtil.getGmailSendMessageTemplateKeyboard())
                            .parseMode(ParseMode.MARKDOWN)
                            .build()
            );

            throw new GmailException();
        }

        return informationMapper.mapEmailMessageDtoToMime(
                EmailMessageDto.builder()
                        .from(fromEmail)
                        .to(splitRawMessage[0])
                        .subject(checkByEmptyFunc.apply(splitRawMessage[1]))
                        .bodyText(checkByEmptyFunc.apply(splitRawMessage[2]))
                        .build()
        );
    }

    private StringBuilder createPreparedHeadersPart(List<MessagePartHeader> headers) {
        StringBuilder preparedMessage = new StringBuilder("Email found:\n");

        headers.stream()
                .filter(header -> REQUIRED_HEADER_NAMES.contains(header.getName()))
                .forEachOrdered(header -> {
                    String headerName = header.getName();
                    String value = headerName.equals("Date")
                            ? header.getValue().replaceAll("\\+\\d+", EMPTY)
                            : header.getValue();

                    preparedMessage
                            .append(NEW_LINE)
                            .append(headerName)
                            .append(": ")
                            .append(value);
                });

        return preparedMessage;
    }

    private boolean filterBody(MessagePart messagePart) {
        MessagePartBody body = messagePart.getBody();
        String mimeTypePart = messagePart.getMimeType();

        return Optional.ofNullable(body)
                .map(b -> (mimeTypePart.equals(MediaType.TEXT_PLAIN_VALUE) || mimeTypePart.equals(MediaType.TEXT_HTML_VALUE))
                        && !b.getData().isBlank())
                .orElse(false);
    }

    private String createFullEmailMessage(StringBuilder preparedHeaders, String body) {
        String decodedMessage = new String(Base64.getUrlDecoder().decode(body.getBytes(UTF_8)), UTF_8);
        String abbreviatedMessage = decodedMessage
                .replaceAll(LINK_REGEXP, PREPARED_LINK)
                .replaceAll(HTML_TAG_REGEXP, EMPTY)
                .replaceAll(HTML_WHITESPACES_REGEXP, NEW_LINE)
                .replaceAll(REDUNDANT_SPACES_REGEXP, NEW_LINE);

        return preparedHeaders
                .append("\n\nMessage:\n")
                .append(abbreviatedMessage)
                .toString();
    }

}
