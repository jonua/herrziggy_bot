package me.jonua.herrziggy_bot.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public final class DateTimeUtils {
    public static final String FORMAT_FULL = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String FORMAT_FULL_DATE_SHORT_TIME = "MMM d, yyyy HH:mm";
    public static final String FORMAT_SHORT_DATE_WITH_DAY_NAME = "EEE, MMM d";
    public static final String FORMAT_SHORT_TIME = "HH:mm";

    public static String formatDate(ZonedDateTime zdt, String pattern) {
        return formatDate(zdt, ZoneId.systemDefault(), pattern);
    }

    public static String formatDate(ZonedDateTime zdt, ZoneId zoneId, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.ofInstant(zdt.toInstant(), zoneId));
    }

    public static ZonedDateTime getLastDateTimeOfWeek(ZonedDateTime zdt) {
        Calendar instance = Calendar.getInstance();
        instance.set(zdt.getYear(), zdt.getMonthValue() - 1, zdt.getDayOfMonth(), 23, 59, 59);
        instance.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().getFirstDayOfWeek() + 7);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("Europe/Moscow"));
    }

    public static ZonedDateTime getEndOfNextDay(ZonedDateTime zdt) {
        Calendar instance = Calendar.getInstance();
        ZonedDateTime nextDate = zdt.plusDays(1);
        instance.set(nextDate.getYear(), nextDate.getMonthValue() - 1, nextDate.getDayOfMonth(), 23, 59, 59);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("Europe/Moscow"));
    }

    public static ZonedDateTime getEndOfDay(ZonedDateTime zdt) {
        Calendar instance = Calendar.getInstance();
        instance.set(zdt.getYear(), zdt.getMonthValue() - 1, zdt.getDayOfMonth(), 23, 59, 59);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("UTC"));
    }
}
