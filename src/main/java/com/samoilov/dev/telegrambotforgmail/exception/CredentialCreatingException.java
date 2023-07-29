package com.samoilov.dev.telegrambotforgmail.exception;

public class CredentialCreatingException extends RuntimeException {

    private static final String REASON = "Failed to create Credential";

    public CredentialCreatingException(Throwable ex) {
        super(REASON, ex);
    }

}
