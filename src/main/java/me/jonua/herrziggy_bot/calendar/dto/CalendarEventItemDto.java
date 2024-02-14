package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
public class CalendarEventItemDto {
    private static final Pattern PATTERN_SUMMARY_WITHOUT_GROUP_DISCRIMINATOR = Pattern.compile("^(.+)(\\([\\d]{1,} [а-яА-Я]+\\))$");

    private String kind;
    private String etag;
    private String id;
    private String status;
    private String htmlLink;
    private ZonedDateTime created;
    private ZonedDateTime updated;
    private String summary;
    private EventPersonDto creator;
    private EventOrganizerDto organizer;
    private EventDateTimeDto start;
    private EventDateTimeDto end;
    private String iCalUID;
    private Long sequence;
    private String eventType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarEventItemDto that = (CalendarEventItemDto) o;
        return Objects.equals(kind, that.kind) &&
                Objects.equals(status, that.status) &&
                compareSummary(that) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    private boolean compareSummary(CalendarEventItemDto that) {
        String thisSummary = getSummaryByPatternOrOriginSummary(summary).replace(" ", "").toLowerCase();
        String thatSummary = getSummaryByPatternOrOriginSummary(that.summary).replace(" ", "").toLowerCase();

        return thisSummary.equals(thatSummary);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(
                kind,
                status,
                getSummaryByPatternOrOriginSummary(summary).replace(" ", "").toLowerCase(Locale.ROOT),
                start,
                end);
        log.warn("summary {} kind {} status {} start {} end {} hash {}", summary, kind, status, start, end, hash);
        return hash;
    }

    private String getSummaryByPatternOrOriginSummary(String summary) {
        Matcher matcher = CalendarEventItemDto.PATTERN_SUMMARY_WITHOUT_GROUP_DISCRIMINATOR.matcher(summary);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return summary;
    }
}
