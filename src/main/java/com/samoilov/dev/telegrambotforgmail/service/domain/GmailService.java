package com.samoilov.dev.telegrambotforgmail.service.domain;

import com.google.api.services.gmail.Gmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailService {

    private final Gmail gmail;

}
