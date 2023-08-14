package com.samoilov.dev.telegrambotforgmail.api.exception;

public class UserNotFoundException extends RuntimeException {

        private static final String REASON = "User not found";

        public UserNotFoundException() {
            super(REASON);
        }

}
