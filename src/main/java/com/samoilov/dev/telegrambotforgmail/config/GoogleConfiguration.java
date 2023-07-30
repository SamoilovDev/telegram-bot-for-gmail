package com.samoilov.dev.telegrambotforgmail.config;

import com.samoilov.dev.telegrambotforgmail.config.properties.GoogleProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GoogleConfiguration {

    private final GoogleProperties googleProperties;

}
