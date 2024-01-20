package me.jonua.herrziggy_bot.calendar;

import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitGoogleCalendarApi {
    @GET("calendars/{calendarId}/events")
    Call<CalendarEventsDto> getEvents(@Path("calendarId") String calendarId, @Query("timeMin") String timeMin, @Query("timeMax") String timeMax);

    @GET("calendars/{calendarId}/events")
    Call<CalendarEventsDto> searchEvents(@Path("calendarId") String calendarId, @Query("timeMin") String timeMin, @Query("timeMax") String timeMax, @Query("q") String q);
}
