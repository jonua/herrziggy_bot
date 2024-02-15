package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.GoogleCalendarApi;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.enums.calendar.CalendarPeriod;
import me.jonua.herrziggy_bot.model.CalendarNotificationConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.jonua.herrziggy_bot.utils.DateTimeUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarService {
    private static final String STREAM_NAME_PATTERN = "^(.+)\\(\\d?\\s+([потокПОТОК]{5,})\\)$";
    private static final Pattern STREAM_NAME_COMPILED_PATTERN = Pattern.compile(STREAM_NAME_PATTERN);

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

        Objects.requireNonNull(calendarId);
        return googleCalendarApi.searchEvents(calendarId, timeMin, timeMax, q);
    }

    @Transactional
    public List<CalendarEventItemDto> getMergedCalendarsEvents(CalendarPeriod calendarPeriod, List<String> calendarIds) {
        return getMergedCalendarsEvents(calendarPeriod, calendarIds, null);
    }

    @Transactional
    public List<CalendarEventItemDto> getMergedCalendarsEvents(CalendarPeriod calendarPeriod, List<String> calendarIds, String q) {
        return calendarIds.stream()
                .map(calendarId -> getCalendarEvents(calendarPeriod, calendarId, q))
                .map(events -> {
                    if (calendarIds.size() > 1) {
                        return removeStreamNameFromSummary(events);
                    } else {
                        return events;
                    }
                })
                .map(CalendarEventsDto::getItems)
                .flatMap(List::stream)
                .distinct()
                .sorted(Comparator.comparing(e -> e.getStart().getDateTime()))
                .collect(Collectors.toList());
    }

    private CalendarEventsDto removeStreamNameFromSummary(CalendarEventsDto events) {
        events.getItems()
                .forEach(item -> {
                    Matcher matcher = STREAM_NAME_COMPILED_PATTERN.matcher(item.getSummary().trim());
                    if (matcher.find()) {
                        item.setSummary(matcher.group(1).trim());
                    }
                });
        return events;
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
    public List<CalendarNotificationConfiguration> findActiveConfigurations() {
        return calendarRepository.findActiveConfigurations();
    }

    @Transactional
    public CalendarNotificationConfiguration getNotificationConfiguration(String configUuid) {
        return calendarRepository.findNotificationConfiguration(configUuid)
                .orElseThrow(() -> {
                    log.error("No notification configuration with uuid {} was found", configUuid);
                    return new RuntimeException("No notification configuration found");
                });
    }

    @Transactional
    public List<String> findGoogleCalendarIdsByUser(String tgUserId) {
        return calendarRepository.findGoogleCalendarIdsByUser(tgUserId);
    }

    @Transactional
    public String getCalendarNameByUser(String tgUserId) {
        return calendarRepository.findCalendarNameByUser(tgUserId)
                .orElseGet(() -> {
                    log.error("No calendars found for user {}", tgUserId);
                    return "";
                });
    }
}
