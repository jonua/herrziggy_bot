package me.jonua.herrziggy_bot.calendar;

import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitGoogleCalendarApi {
    @GET("events")
    Call<CalendarEventsDto> getEvents(@Query("timeMin") String timeMin, @Query("timeMax") String timeMax);

    @GET("events")
    Call<CalendarEventsDto> searchEvents(@Query("timeMin") String timeMin, @Query("timeMax") String timeMax, @Query("q") String q);
}
