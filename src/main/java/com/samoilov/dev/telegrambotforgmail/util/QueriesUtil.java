package com.samoilov.dev.telegrambotforgmail.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class QueriesUtil {

    String ANYWHERE = " in:anywhere";
    String IMPORTANT = " is:important";
    String STARRED = " is:starred";
    String ATTACHMENT = " has:attachment";
    String UNREAD = " is:unread";
    String READ = " is:read";
    String INBOX = " in:inbox";
    String TRASH = " in:trash";
    String SPAM = "in:spam";
    String OLDER_THAN = " older_than:"; // format: YYYY/MM/DD
    String NEWER_THAN = " newer_than:"; // format: YYYY/MM/DD
    String SUBJECT = " subject:"; // find by text in subject
    String FROM = " from:"; // find by email address
    String TO = " to:"; // find by email address

}
