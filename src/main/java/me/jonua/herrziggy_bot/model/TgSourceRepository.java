package me.jonua.herrziggy_bot.model;

import me.jonua.herrziggy_bot.data.jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TgSourceRepository extends BaseRepository<TgSource> {
    Optional<TgSource> findBySourceId(String s);
}