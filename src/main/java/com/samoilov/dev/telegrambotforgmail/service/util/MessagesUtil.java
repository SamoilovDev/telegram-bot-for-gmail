package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessagesUtil {

    public static final String START = """
           Hello, %s!
           I'm Gmail Bot. I can help you to send emails from your Gmail account.
           To start using me you need to authorize me. To do this type /authorize and go to link below message.
           To see information about me type /info.
           You can see all commands in the menu at the bottom left.
           """;

    public static final String INFO = """
            This bot was created by [Vladimir Samoilov](https://t.me/samoilov_vl).
            """;

    public static final String AUTHORIZE = """
             To start using me you need to authorize me. To do this go to the link below and follow the instructions.
             """;

    public static final String SUCCESS_AUTHORIZATION = """
            You have successfully authorized me. Now you can get and send emails from your Gmail account.
            """;

    public static final String AUTHORIZATION_FAILED = """
            Sorry, internal error during authorization, please try to authorize me again or try again later.
            """;

    public static final String SETTINGS = """
            To change settings choose and click on the button below message.
            """;

    public static final String SETTINGS_STATS = """
            %s
            Count of commands: %s
            Active type of your account: %s
            """;

    public static final String SETTINGS_DELETE = """
            Your account's data was successful deleted! :(
            Goodbye, %s
            """;

    public static final String GMAIL = """
            To get messages from your Gmail account click on the button below message.
            """;

    public static final String SEND = """
            To send email, please, use this example and change <?> to your email data.
            _Tip: if you want to skip something, just write '-' (You can not skip email:to)_
            /send <email:to> -> <subject> -> <body>
            """;

    public static final String SEND_FINISH = """
            You have successfully sent email!
            """;

    public static final String SEND_ERROR = """
            Error during sending message, please try again.
            _Tip: check if you used the template correctly_
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
