package com.samoilov.dev.telegrambotforgmail.entity;

import com.samoilov.dev.telegrambotforgmail.enums.ActiveType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@SQLDelete(sql = "UPDATE user_table SET active_type = 'DISABLED' WHERE id = ?")
@Table(name = "user_table", uniqueConstraints = {
        @UniqueConstraint(columnNames = "telegram_id", name = "user_table_unique_telegram_id_idx"),
        @UniqueConstraint(columnNames = "gmail_address", name = "user_table_unique_gmail_address_idx")
})
public class UserEntity {

    @Id
    @GenericGenerator(name = "UUID")
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_id", length = 50)
    private String id;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "telegram_id", length = 50)
    private Long telegramId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "user_name", length = 50)
    private String userName;

    @Email(regexp = "^[\\w\\d+_.-]+@gmail.com$")
    @Column(name = "gmail_address", length = 50)
    private String email;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "active_type")
    private ActiveType activeType = ActiveType.ENABLED;

    @Builder.Default
    @Column(name = "command_counter")
    private Long commandCounter = 0L;

}
