package com.samoilov.dev.telegrambotforgmail.api.service.checker;

import com.google.api.services.gmail.Gmail;
import com.samoilov.dev.telegrambotforgmail.api.service.domain.GmailService;
import com.samoilov.dev.telegrambotforgmail.api.service.domain.UserService;
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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class NewEmailsCheckerService {

    private final UserService userService;

    private final GmailService gmailService;

    private final CacheManager cacheManager;

    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60000)
    private void checkNewEmails() {
        Cache cache = Objects.requireNonNull(cacheManager.getCache("gmail"));

        userService.getAllChatIds()
                .forEach(chatId -> Optional
                        .ofNullable(cache.get(chatId, Gmail.class))
                        .ifPresent(gmail -> this.publishNewUnreadMessage(chatId, gmail))
                );
    }

    private void publishNewUnreadMessage(Long chatId, Gmail gmail) {
        String lastUnreadMessage = Optional.of(gmailService.getMessagesByQuery(gmail, QueriesUtil.UNREAD, chatId))
                .filter(unreadMessages -> !unreadMessages.isEmpty())
                .map(unreadMessages -> unreadMessages.get(0))
                .orElse(EMPTY);
        Matcher matcher = Pattern.compile(RegexpUtil.EMAIL_DATE_REGEXP)
                .matcher(lastUnreadMessage);

        if (matcher.find()) {
            String[] foundedSplitDate = matcher.group().split(RegexpUtil.WHITESPACES);
            LocalTime sendingTime = LocalTime.parse(
                    foundedSplitDate[foundedSplitDate.length - 1],
                    DateTimeFormatter.ofPattern(RegexpUtil.TIME)
            );

            if (sendingTime.isAfter(LocalTime.now().minusMinutes(2L))) {
                eventPublisher.publishEvent(new SendMessage(String.valueOf(chatId), lastUnreadMessage));
            }
        }
    }

}
