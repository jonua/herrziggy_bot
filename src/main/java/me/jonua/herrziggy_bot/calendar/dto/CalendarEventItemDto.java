package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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
}
