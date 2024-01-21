package me.jonua.herrziggy_bot.data.jpa.repository;

import me.jonua.herrziggy_bot.model.Calendar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarRepository extends BaseRepository<Calendar> {
    @Query("SELECT c FROM Calendar c WHERE c = (SELECT u.calendar FROM TgUser u WHERE u.userId = :tgUserId)")
    Optional<Calendar> findByUserId(String tgUserId);

    @Query("SELECT c FROM Calendar c WHERE c = (SELECT s.calendar FROM TgSource s WHERE s.sourceId = :sourceId)")
    Optional<Calendar> findBySourceId(String sourceId);

    Optional<Calendar> findByUuid(String calendarUuid);
}