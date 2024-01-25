package me.jonua.herrziggy_bot.model;

import me.jonua.herrziggy_bot.data.jpa.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TgSourceRepository extends BaseRepository<TgSource> {
    @Query("SELECT s FROM TgSource s WHERE s.sourceId = :sourceId")
    Optional<TgSource> findBySourceId(String sourceId);

    @Modifying
    @Query("UPDATE TgSource SET migrateFromChatId = :destinationChatId, sourceId = :newSourceId WHERE sourceId = :destinationChatId")
    void updateMigrateToChatId(String destinationChatId, String newSourceId);
}