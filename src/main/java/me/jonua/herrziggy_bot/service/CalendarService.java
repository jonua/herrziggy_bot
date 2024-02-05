package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.GoogleCalendarApi;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.enums.calendar.CalendarPeriod;
import me.jonua.herrziggy_bot.model.CalendarNotificationConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static me.jonua.herrziggy_bot.utils.DateTimeUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;
    @Value("${bot.locale}")
    private Locale locale;
    private final GoogleCalendarApi googleCalendarApi;

    public CalendarEventsDto getCalendarEvents(CalendarPeriod calendarPeriod, String calendarId) {
        return getCalendarEvents(calendarPeriod, calendarId, null);
    }

    public CalendarEventsDto getCalendarEvents(CalendarPeriod calendarPeriod, String calendarId, String q) {
        log.trace("Command will be executed on a calendar: {} with period:{} and query:{}",
                calendarId, calendarPeriod, q);

        ZonedDateTime now = ZonedDateTime.now();
        Pair<ZonedDateTime, ZonedDateTime> periodDates = getPeriod(calendarPeriod, now);

        String timeMin = formatDate(periodDates.getFirst(), locale, FORMAT_FULL);
        String timeMax = formatDate(periodDates.getSecond(), locale, FORMAT_FULL);

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
            case FULL_SEMESTER_SEMINARS -> Pair.of(getStartOfDay(subjectDate), getEndOfHalfYear(subjectDate));
            case FULL_SEMESTER_TESTS -> Pair.of(getStartOfDay(subjectDate), getEndOfHalfYear(subjectDate));
        };
    }

    @Transactional
    public List<CalendarNotificationConfiguration> findActiveSchedules() {
        return calendarRepository.findActiveSchedules();
    }

    @Transactional
    public CalendarNotificationConfiguration getNotificationConfiguration(String configUuid) {
        return calendarRepository.findNotificationConfiguration(configUuid)
                .orElseThrow(() -> {
                    log.error("No notification configuration with uuid {} was found", configUuid);
                    return new RuntimeException("No notification configuration found");
                });
    }
}
