package me.jonua.herrziggy_bot.data.jpa.repository;

import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.model.CalendarNotificationConfiguration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends BaseRepository<CalendarConfiguration> {
    @Query("SELECT cs.googleCalendarId " +
            "FROM TgSource s " +
            "JOIN s.calendarConfiguration cc " +
            "JOIN cc.calendarSources cs " +
            "WHERE s.sourceId = :sourceId AND s.type != null")
    List<String> findGoogleCalendarIdsByUser(String sourceId);

    Optional<CalendarConfiguration> findByUuid(String calendarUuid);

    @Query("SELECT c FROM CalendarNotificationConfiguration c WHERE c.active = true")
    List<CalendarNotificationConfiguration> findActiveConfigurations();

    @Query("SELECT c FROM CalendarNotificationConfiguration c WHERE c.uuid = :configUuid")
    Optional<CalendarNotificationConfiguration> findNotificationConfiguration(String configUuid);

    @Query("SELECT cc.additionalInfo FROM TgSource s JOIN s.calendarConfiguration cc WHERE s.sourceId = :tgUserId")
    Optional<String> findCalendarNameByUser(String tgUserId);
}
