package com.samoilov.dev.telegrambotforgmail.store.repository;

import com.samoilov.dev.telegrambotforgmail.store.entity.GmailEntity;
import com.samoilov.dev.telegrambotforgmail.store.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GmailRepository extends JpaRepository<GmailEntity, Long> {

    @Query(value = "SELECT COUNT(*) > 0 FROM gmails g " +
            "WHERE g.email_address = :email AND g.user_telegram_id = :#{#user.telegramId} " +
            "AND g.user_first_name = :#{#user.firstName}",
            nativeQuery = true)
    boolean existsEmailAddressForCurrentUser(@Param("email") String email, @Param("user") UserEntity user);

}
