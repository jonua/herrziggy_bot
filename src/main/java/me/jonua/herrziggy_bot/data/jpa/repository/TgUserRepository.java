package me.jonua.herrziggy_bot.data.jpa.repository;

import me.jonua.herrziggy_bot.model.TgUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TgUserRepository extends BaseRepository<TgUser> {
    Optional<TgUser> findByUserId(String id);
}
