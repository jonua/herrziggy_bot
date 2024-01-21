package me.jonua.herrziggy_bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.GoogleCalendarApi;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.enums.calendar.CalendarPeriod;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static me.jonua.herrziggy_bot.utils.DateTimeUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final GoogleCalendarApi googleCalendarApi;

    public CalendarEventsDto getCalendarEvents(CalendarPeriod calendarPeriod, String calendarId) {
        return getCalendarEvents(calendarPeriod, calendarId, null);
    }

    public CalendarEventsDto getCalendarEvents(CalendarPeriod calendarPeriod, String calendarId, String q) {
        log.trace("Command will be executed on a calendar: {} with period:{} and query:{}",
                calendarId, calendarPeriod, q);

        ZonedDateTime now = ZonedDateTime.now();
        Pair<ZonedDateTime, ZonedDateTime> periodDates = getPeriod(calendarPeriod, now);

        String timeMin = formatDate(periodDates.getFirst(), FORMAT_FULL);
        String timeMax = formatDate(periodDates.getSecond(), FORMAT_FULL);

        return googleCalendarApi.searchEvents(calendarId, timeMin, timeMax, q);
    }

    private Pair<ZonedDateTime, ZonedDateTime> getPeriod(CalendarPeriod period, ZonedDateTime subjectDate) {
        return switch (period) {
            case TWO_DAYS -> Pair.of(getStartOfDay(subjectDate), getEndOfNextDay(subjectDate));
            case THIS_WEEK -> Pair.of(getStartOfDay(subjectDate), getLastDateTimeOfWeek(subjectDate));
            case NEXT_WEEK ->
                    Pair.of(getLastDateTimeOfWeek(subjectDate), getLastDateTimeOfWeek(subjectDate).plusWeeks(1));
            case CURRENT_30_DAYS_SEMINARS -> Pair.of(getStartOfDay(subjectDate), getEndOfDay(subjectDate.plusDays(60)));
            case CURRENT_30_DAYS_TESTS -> Pair.of(getStartOfDay(subjectDate), getEndOfDay(subjectDate.plusDays(60)));
        };
    }
}
