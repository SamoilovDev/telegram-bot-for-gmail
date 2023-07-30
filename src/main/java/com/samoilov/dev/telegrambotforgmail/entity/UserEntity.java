package com.samoilov.dev.telegrambotforgmail.entity;

import com.samoilov.dev.telegrambotforgmail.entity.id.TelegramId;
import com.samoilov.dev.telegrambotforgmail.enums.ActiveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
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

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@IdClass(TelegramId.class)
@SQLDelete(sql = "UPDATE user_table SET active_type = 'DISABLED' WHERE telegram_id = ?", check = ResultCheckStyle.COUNT)
@Table(name = "user_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = "gmail_address", name = "user_table_unique_gmail_address_idx")
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

    @Email(regexp = "^[\\w\\d+_.-]+@gmail.com$")
    @Column(name = "gmail_address", length = 50)
    private String email;

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

}
