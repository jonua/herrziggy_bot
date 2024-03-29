package me.jonua.herrziggy_bot.utils;

import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public final class DateTimeUtils {
    public static final String FORMAT_FULL = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String FORMAT_FULL_DATE_SHORT_TIME = "d MMM, yyyy HH:mm";
    public static final String FORMAT_SHORT_DATE_WITH_DAY_NAME = "EEE, d MMM";
    public static final String FORMAT_SHORT_TIME = "HH:mm";
    public static final String FORMAT_SHORT_DATE = "d MMM";

    public static String formatDate(ZonedDateTime zdt, Locale locale, String pattern) {
        return formatDate(zdt, ZoneId.systemDefault(), locale, pattern);
    }

    public static String formatDate(ZonedDateTime zdt, ZoneId zoneId, Locale locale, String pattern) {
        return DateTimeFormatter.ofPattern(pattern, locale).format(ZonedDateTime.ofInstant(zdt.toInstant(), zoneId));
    }

    public static ZonedDateTime getLastDateTimeOfWeek(ZonedDateTime zdt) {
        Calendar instance = Calendar.getInstance();
        instance.setFirstDayOfWeek(Calendar.MONDAY);
        instance.set(zdt.getYear(), zdt.getMonthValue() - 1, zdt.getDayOfMonth(), 23, 59, 59);
        instance.set(Calendar.DAY_OF_WEEK,  Calendar.SUNDAY);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("Europe/Moscow"));
    }

    public static void main(String[] args) {
        System.out.println(DateTimeUtils.getLastDateTimeOfWeek(ZonedDateTime.now()));
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

    public static ZonedDateTime getStartOfDay(ZonedDateTime zdt) {
        Calendar instance = Calendar.getInstance();
        instance.set(zdt.getYear(), zdt.getMonthValue() - 1, zdt.getDayOfMonth(), 0, 0, 0);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("UTC"));
    }

    public static ZonedDateTime getEndOfHalfYear(ZonedDateTime zdt) {
        int currentMonthValue = zdt.getMonthValue();
        Month targetMonthValue = Month.JULY;
        if (currentMonthValue > Month.JULY.getValue()) {
            targetMonthValue = Month.DECEMBER;
        }
        Calendar instance = Calendar.getInstance();
        instance.set(zdt.getYear(), targetMonthValue.getValue() - 1, getLastDayOfMonth(targetMonthValue), 23, 59, 59);
        return ZonedDateTime.ofInstant(instance.toInstant(), ZoneId.of("UTC"));
    }

    public static int getLastDayOfMonth(Month month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month.getValue() - 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}
