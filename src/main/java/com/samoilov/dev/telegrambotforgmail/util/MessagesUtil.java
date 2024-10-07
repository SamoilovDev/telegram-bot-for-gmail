package com.samoilov.dev.telegrambotforgmail.util;

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
            Your session is expired! Please, authorize me again to work with gmail
            """;

    public static final String SETTINGS = """
            To change settings choose and click on the button below message.
            """;

    public static final String SETTINGS_STATS = """
            Your telegram id: %s
            Your first name: %s
            Your last name: %s
            Your user name: %s
            Your first command was sent at: %s
            Count of commands: %s
            Active type of your account: %s
            Authorized emails: %s
            """;

    public static final String SETTINGS_DELETE = """
            Your account's data was successful deleted! :(
            Goodbye, %s
            """;

    public static final String GMAIL = """
            To get or send messages from your Gmail account click on the button below message.
            """;

    public static final String SEND = """
            To send email, please, use this example and change <?> to your email data.
            _Tip: if you want to skip something, just write <-> (You can not skip email:to)_
            /send <Required: email:to> -> <Optional: subject> -> <Optional: body>
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


    public static final String IMPOSSIBLE_TO_AUTHORIZE_NOW = "Impossible to authorize now, please try again later";

}
