package me.jonua.herrziggy_bot.calendar;

import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;

public interface GoogleCalendarApi {
    CalendarEventsDto searchEvents(String timeMin, String timeMax, String q);

    CalendarEventsDto getEvents(String timeMin, String timeMax);
}
