package com.samoilov.dev.telegrambotforgmail.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandType {

    START("/start"),

    INFO("/info"),

    COMMANDS("/commands"),

    AUTHORIZE("/authorize"),

    GMAIL("/gmail"),

    ERROR("-");

    private final String command;

    public static CommandType parseCommand(String command) {
        for (CommandType commandType : CommandType.values()) {
            if (commandType.getCommand().equals(command)) {
                return commandType;
            }
        }
        return ERROR;
    }

}
