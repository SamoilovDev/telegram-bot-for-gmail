package com.samoilov.dev.telegrambotforgmail.exception;

public class GmailException extends RuntimeException {

    private static final String REASON = "Exception occurred while working with Gmail API";

    public GmailException(Throwable ex) {
        super(REASON, ex);
    }

    public GmailException() {
        super(REASON);
    }

}
