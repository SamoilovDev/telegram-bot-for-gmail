package com.samoilov.dev.telegrambotforgmail.service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RegexpUtil {

    public static final String EMAIL_REGEXP = "^[\\w.-]+@[\\w.-]+\\.\\w+$";

    public static final String LINK_REGEXP = "^https?://\\S+$";

    public static final String HTML_TAG_REGEXP = "<[^>]*>";

    public static final String HTML_WHITESPACES_REGEXP = "(&nbsp;)+";

    public static final String REDUNDANT_SPACES_REGEXP = "(\\n|\\s){3,}";

    public static final String PREPARED_LINK = "[*click*]($0)";

    public static final String NEXT_MAIL_POINT_REGEXP = "\\s*->\\s*";

    public static final String NOTHING = "<->";

    public static final String NEW_LINE = "\n";

}
