package me.jonua.herrziggy_bot.data.jpa.repository;

import me.jonua.herrziggy_bot.mail.MailConfiguration;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailConfigurationRepository extends BaseRepository<MailConfiguration> {
    @Query("SELECT c FROM MailConfiguration c WHERE c.active = true")
    List<MailConfiguration> findAllActive();
}