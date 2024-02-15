package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Objects;

@Slf4j
@Getter
@Setter
public class CalendarEventItemDto {
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
                Objects.equals(summary, that.summary) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(
                kind,
                status,
                summary,
                start,
                end);
        log.warn("summary {} kind {} status {} start {} end {} hash {}", summary, kind, status, start, end, hash);
        return hash;
    }
}
