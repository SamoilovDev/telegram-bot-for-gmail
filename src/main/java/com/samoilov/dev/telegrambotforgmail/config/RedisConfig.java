package com.samoilov.dev.telegrambotforgmail.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samoilov.dev.telegrambotforgmail.dto.AuthenticationInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<Long, AuthenticationInfoDto> redisTemplate(
            JedisConnectionFactory jedisConnectionFactory,
            ObjectMapper objectMapper) {
        RedisTemplate<Long, AuthenticationInfoDto> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new GenericToStringSerializer<>(Long.class));
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, AuthenticationInfoDto.class));

        return redisTemplate;
    }

}
