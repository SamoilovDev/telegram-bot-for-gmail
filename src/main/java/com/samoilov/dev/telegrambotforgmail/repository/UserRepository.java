package com.samoilov.dev.telegrambotforgmail.repository;

import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.commandCounter = u.commandCounter + 1 WHERE u.telegramId = :telegramId")
    void incrementCount(@Param("telegramId") Long telegramId);

    boolean existsByTelegramId(Long telegramId);

    Optional<UserEntity> findByTelegramId(Long telegramId);

}
