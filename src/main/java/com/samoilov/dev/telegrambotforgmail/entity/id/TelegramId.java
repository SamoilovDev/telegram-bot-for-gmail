package com.samoilov.dev.telegrambotforgmail.entity.id;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Embeddable
@EqualsAndHashCode
public class TelegramId implements Serializable {

    private Long telegramId;

    private String firstName;

}
