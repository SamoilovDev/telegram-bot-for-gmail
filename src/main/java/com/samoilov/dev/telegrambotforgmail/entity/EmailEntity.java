package com.samoilov.dev.telegrambotforgmail.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "email_table")
public class EmailEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(regexp = "^[\\w\\d+_.-]+@gmail.com$")
    @Column(name = "gmail_address", length = 50)
    private String email;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "user_telegram_id", referencedColumnName = "telegram_id"),
            @JoinColumn(name = "user_first_name", referencedColumnName = "first_name")
    })
    private UserEntity user;

}
