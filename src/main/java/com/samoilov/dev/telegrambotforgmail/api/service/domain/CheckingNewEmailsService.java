package com.samoilov.dev.telegrambotforgmail.api.service.domain;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.api.service.util.QueriesUtil;
import com.samoilov.dev.telegrambotforgmail.api.service.util.RegexpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class CheckingNewEmailsService {

    private final CacheManager cacheManager;

    private final ApplicationEventPublisher eventPublisher;

    private final UserService userService;

    private final GmailService gmailService;

    @Scheduled(fixedRate = 60000)
    private void checkNewEmails() {
        Cache cache = Objects.requireNonNull(cacheManager.getCache("gmail"));
        Map<Long, Gmail> gmailMap = new HashMap<>();

        userService
                .getAllChatIds()
                .forEach(chatId -> gmailMap.put(chatId, cache.get(chatId, Gmail.class)));

        gmailMap.forEach((chatId, gmail) -> {
            if (Objects.isNull(chatId) || Objects.isNull(gmail)) {
                return;
            }

            String lastUnreadMessage = gmailService.getMessagesByQuery(gmail, QueriesUtil.UNREAD, chatId).get(0);
            Matcher matcher = Pattern.compile(RegexpUtil.EMAIL_DATE_REGEXP).matcher(lastUnreadMessage);

            if (matcher.find()) {
                String foundedDate = matcher.group();
                LocalTime sendingTime = LocalTime.parse(
                        foundedDate.split(RegexpUtil.WHITESPACES)[5],
                        DateTimeFormatter.ofPattern(RegexpUtil.TIME)
                );

                if (sendingTime.isAfter(LocalTime.now().minusMinutes(2L))) {
                    eventPublisher.publishEvent(
                            SendMessage
                                    .builder()
                                    .chatId(chatId)
                                    .text(lastUnreadMessage)
                                    .build()
                    );
                }
            }
        });

    }

}
