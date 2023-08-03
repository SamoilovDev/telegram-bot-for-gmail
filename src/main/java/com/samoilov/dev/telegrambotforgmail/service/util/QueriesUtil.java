package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueriesUtil {

    public static final String ANYWHERE = " in:anywhere";

    public static final String IMPORTANT = " is:important";

    public static final String STARRED = " is:starred";

    public static final String ATTACHMENT = " has:attachment";

    public static final String UNREAD = " is:unread";

    public static final String READ = " is:read";

    public static final String INBOX = " in:inbox";

    public static final String TRASH = " in:trash";

    public static final String SPAM = "in:spam";

    public static final String OLDER_THAN = " older_than:"; // format: YYYY/MM/DD

    public static final String NEWER_THAN = " newer_than:"; // format: YYYY/MM/DD

    public static final String SUBJECT = " subject:"; // find by text in subject

    public static final String FROM = " from:"; // find by email address

    public static final String TO = " to:"; // find by email address


}
