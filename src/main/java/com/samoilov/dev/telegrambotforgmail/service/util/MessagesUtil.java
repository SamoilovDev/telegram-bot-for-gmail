package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessagesUtil {

    public static final String START = """
           Hello, %s!
           I'm Gmail Bot. I can help you to send emails from your Gmail account.
           To start using me you need to authorize me. To do this go to the link below and follow the instructions.
           To see all commands type /commands. To see information about me type /info.
           (Or click on the buttons below)
           """;

    public static final String COMMANDS = """
            /start - start the bot
            /info - information about the bot
            /commands - list of all commands
            /authorize - get authorization link
            """;

    public static final String INFO = """
            This bot was created by @samoilov_vl.
            """;

    public static final String AUTHORIZE = """
             To start using me you need to authorize me. To do this go to the link below and follow the instructions.
             """;

    public static final String SUCCESS_AUTHORIZATION = """
            You have successfully authorized me. Now you can send emails from your Gmail account.
            """;

    public static final String GMAIL = """
            To get messages from your Gmail account click on the button below message.
            """;

    public static final String SEND = """
            -
            """;

    public static final String GET = """
            To get messages from your Gmail account choose and click on the button below message.
            """;

    public static final String GET_FINISH = """
            You have successfully got last messages from your Gmail account at this category.
            """;

    public static final String ERROR = """
            Sorry, I don't understand you.
            """;

}
