package me.jonua.herrziggy_bot.data.jpa.repository;

import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.model.CalendarNotificationConfiguration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends BaseRepository<Calendar> {
    @Query("SELECT c FROM Calendar c WHERE c = (SELECT s.calendar FROM TgSource s WHERE s.sourceId = :sourceId AND s.type != null)")
    Optional<Calendar> findBySourceId(String sourceId);

    Optional<Calendar> findByUuid(String calendarUuid);

    @Query("SELECT c FROM CalendarNotificationConfiguration c WHERE c.active = true")
    List<CalendarNotificationConfiguration> findActiveSchedules();

    @Query("SELECT c FROM CalendarNotificationConfiguration c WHERE c.uuid = :configUuid")
    Optional<CalendarNotificationConfiguration> findNotificationConfiguration(String configUuid);
}
