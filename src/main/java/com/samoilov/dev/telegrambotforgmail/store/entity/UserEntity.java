package com.samoilov.dev.telegrambotforgmail.store.entity;

import com.samoilov.dev.telegrambotforgmail.store.entity.base.BaseEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.id.TelegramId;
import com.samoilov.dev.telegrambotforgmail.store.enums.ActiveType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TelegramId.class)
@SuperBuilder(toBuilder = true)
@SQLDelete(
        sql = "UPDATE users SET active_type = 'DISABLED' WHERE telegram_id = ?",
        check = ResultCheckStyle.COUNT
)
@Table(name = "users",
        indexes = {
                @Index(columnList = "telegram_id", name = "users_telegram_id_idx"),
                @Index(columnList = "username", name = "users_username_idx")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username", name = "users_unique_username_idx")
        })
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "telegram_id", length = 50)
    private Long telegramId;

    @Id
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "username", length = 50)
    private String username;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "active_type")
    private ActiveType activeType = ActiveType.ENABLED;

    @Builder.Default
    @Column(name = "command_counter")
    private Long commandCounter = 0L;

    @Builder.Default
    @ElementCollection
    private List<Long> chatIds = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GmailEntity> emails = new ArrayList<>();

}
