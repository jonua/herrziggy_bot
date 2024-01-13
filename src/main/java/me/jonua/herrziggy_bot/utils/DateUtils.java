package me.jonua.herrziggy_bot.utils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    public static String formatDate(Instant instant, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(instant);
    }
}
