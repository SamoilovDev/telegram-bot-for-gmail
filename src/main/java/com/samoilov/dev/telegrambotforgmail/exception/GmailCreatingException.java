package com.samoilov.dev.telegrambotforgmail.exception;

public class GmailCreatingException extends RuntimeException {

    private static final String REASON = "Failed to create credentials or gmail object";

    public GmailCreatingException(Throwable ex) {
        super(REASON, ex);
    }

}
