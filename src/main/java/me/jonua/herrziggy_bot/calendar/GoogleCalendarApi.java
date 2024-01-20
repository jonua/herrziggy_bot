package me.jonua.herrziggy_bot.calendar;

import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;

public interface GoogleCalendarApi {
    CalendarEventsDto searchEvents(String calendarId, String timeMin, String timeMax, String q);

    CalendarEventsDto getEvents(String calendarId, String timeMin, String timeMax);
}
