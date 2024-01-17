package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.utils.RetrofitUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleCalendarApiService implements GoogleCalendarApi {
    private final RetrofitGoogleCalendarApi calendarApi;

    @Override
    @Cacheable(cacheNames = "calendar-events", key = "{#timeMax}")
    public CalendarEventsDto getEvents(String timeMin, String timeMax) {
        return RetrofitUtils.executeWithResult(calendarApi.getEvents(timeMin, timeMax));
    }

    @Override
    @Cacheable(cacheNames = "calendar-events", key = "{#timeMax,#q}")
    public CalendarEventsDto searchEvents(String timeMin, String timeMax, String q) {
        return RetrofitUtils.executeWithResult(calendarApi.searchEvents(timeMin, timeMax, q));
    }
}
