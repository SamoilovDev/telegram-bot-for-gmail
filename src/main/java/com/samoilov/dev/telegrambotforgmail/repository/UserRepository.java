package com.samoilov.dev.telegrambotforgmail.repository;

import com.samoilov.dev.telegrambotforgmail.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    @Modifying
    @Query(value = "SELECT u FROM UserEntity u WHERE u.id = :id AND u.commandCounter = u.commandCounter + 1")
    void incrementCount(@Param("id") String userId);

}
