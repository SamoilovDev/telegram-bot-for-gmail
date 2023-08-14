package com.samoilov.dev.telegrambotforgmail.store.entity;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@IdClass(TelegramId.class)
@SQLDelete(
        sql = "UPDATE user_table SET active_type = 'DISABLED' WHERE telegram_id = ?",
        check = ResultCheckStyle.COUNT
)
@Table(name = "user_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_name", name = "user_table_unique_user_name_idx")
})
public class UserEntity {

    @Id
    @Column(name = "telegram_id", length = 50)
    private Long telegramId;

    @Id
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "user_name", length = 50)
    private String userName;

    @CreationTimestamp
    @Column(name = "registered_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "active_type")
    private ActiveType activeType = ActiveType.ENABLED;

    @Builder.Default
    @Column(name = "command_counter")
    private Long commandCounter = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EmailEntity> emails = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> chatIds = new ArrayList<>();

}
