package me.jonua.herrziggy_bot.model;

import me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection;
import me.jonua.herrziggy_bot.data.jpa.repository.BaseRepository;
import me.jonua.herrziggy_bot.enums.Gender;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TgSourceRepository extends BaseRepository<TgSource> {
    @Query("SELECT s FROM TgSource s WHERE s.sourceId = :sourceId")
    Optional<TgSource> findBySourceId(String sourceId);

    @Modifying
    @Query("UPDATE TgSource SET migrateFromChatId = :destinationChatId, sourceId = :newSourceId WHERE sourceId = :destinationChatId")
    void updateMigrateToChatId(String destinationChatId, String newSourceId);

    @Query("SELECT s FROM TgSource s WHERE s.type='private' AND s.gender = :gender AND s.updateDate >= :updateDateLowerBoundary")
    List<TgSource> findPrivateSources(Gender gender, Date updateDateLowerBoundary);

    @Modifying
    @Query("UPDATE TgSource SET kicked = true, updateDate = CURRENT_TIMESTAMP WHERE sourceId = :sourceId AND kicked = false")
    void markAsKicked(Long sourceId);

    @Modifying
    @Query("UPDATE TgSource SET kicked = false, updateDate = CURRENT_TIMESTAMP WHERE sourceId = :sourceId AND kicked = true")
    void markAsUnKicked(Long sourceId);

    @Query("SELECT new me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection(date_trunc('day', s.createDate), s.type, s.firstName, s.lastName, s.username, s.title) " +
            "FROM TgSource s WHERE s.createDate >= :leftDate " +
            "ORDER BY s.createDate DESC")
    List<TgSourceProjection> getStatNewUsers(Date leftDate);

    @Query("SELECT new me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection(date_trunc('day', s.updateDate), s.type, s.firstName, s.lastName, s.username, s.title) " +
            "FROM TgSource s WHERE s.updateDate >= :leftDate " +
            "ORDER BY s.updateDate DESC")
    List<TgSourceProjection> getStatActiveUsers(Date leftDate);
}
