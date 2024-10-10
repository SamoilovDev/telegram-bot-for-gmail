package com.samoilov.dev.telegrambotforgmail.store.repository;

import com.samoilov.dev.telegrambotforgmail.store.entity.GmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GmailRepository extends JpaRepository<GmailEntity, Long> {

    boolean existsByEmailAddress(String email);

}
