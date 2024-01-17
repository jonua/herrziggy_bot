package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
public class EventDateTimeDto {
    private ZonedDateTime dateTime;
    private ZoneId timeZone;
}
