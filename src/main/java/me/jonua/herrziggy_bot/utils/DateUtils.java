package me.jonua.herrziggy_bot.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    public static String formatDate(ZonedDateTime instant, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(instant);
    }
}
