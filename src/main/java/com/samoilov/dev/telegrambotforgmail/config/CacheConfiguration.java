package com.samoilov.dev.telegrambotforgmail.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfiguration {

    public static final String AUTHENTICATION_INFO_CACHE_NAME = "authenticationInfo";
    public static final String CACHE_MANAGER_BEAN_NAME = "cacheManager";
    public static final String GMAIL_CACHE_NAME = "gmail";

    @Bean
    public CacheManager cacheManager(
            @Value("${spring.cache.expiration}") Integer expiration,
            @Value("${spring.cache.initial-capacity}") Integer initialCapacity,
            @Value("${spring.cache.maximum-size}") Integer maximumSize) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();

        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(expiration, TimeUnit.MINUTES)
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize));
        caffeineCacheManager.setCacheNames(List.of(GMAIL_CACHE_NAME, AUTHENTICATION_INFO_CACHE_NAME));

        return caffeineCacheManager;
    }

}
