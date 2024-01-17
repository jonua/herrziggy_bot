package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CalendarEventsDto {
    private String kind;
    private String etag;
    private String summary;
    private String description;
    private ZonedDateTime updated;
    private ZoneId timeZone;
    private String accessRole;
    private List<Object> defaultReminders;
    private List<CalendarEventItemDto> items;
}
