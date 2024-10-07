package com.samoilov.dev.telegrambotforgmail.exception;

public class AuthorizationUrlCreatingException extends RuntimeException {

    private static final String REASON = "Failed to create authorization url";

    public AuthorizationUrlCreatingException(Throwable ex) {
        super(REASON, ex);
    }

}
