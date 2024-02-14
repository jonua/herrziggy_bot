package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Setter
public class EventDateTimeDto {
    private ZonedDateTime dateTime;
    private ZoneId timeZone;

    @Override
    public String toString() {
        return "EventDateTimeDto{" +
                "dateTime=" + dateTime +
                ", timeZone=" + timeZone +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDateTimeDto that = (EventDateTimeDto) o;
        return Objects.equals(dateTime, that.dateTime) && Objects.equals(timeZone, that.timeZone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, timeZone);
    }
}
