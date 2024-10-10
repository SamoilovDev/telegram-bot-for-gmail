package com.samoilov.dev.telegrambotforgmail.store.entity;

import com.samoilov.dev.telegrambotforgmail.store.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Table(name = "gmails", indexes = {
        @Index(name = "idx_gmails_email_address", columnList = "email_address"),
        @Index(name = "idx_gmails_user", columnList = "user_telegram_id, user_first_name")
})
public class GmailEntity extends BaseEntity {

    @Id
    @Column(name = "email_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(regexp = "^[\\w\\d+_.-]+@gmail.com$")
    @Column(name = "email_address", length = 50, unique = true, nullable = false)
    private String emailAddress;

    @Column(name = "sent_emails_count")
    private Long sentEmailsCount;

    @Column(name = "received_emails_count")
    private Long receivedEmailsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(
                    name = "user_telegram_id",
                    referencedColumnName = "telegram_id",
                    foreignKey = @ForeignKey(name = "fk_gmails_user")),
            @JoinColumn(
                    name = "user_first_name",
                    referencedColumnName = "first_name",
                    foreignKey = @ForeignKey(name = "fk_gmails_user"))
    })
    private UserEntity user;

}