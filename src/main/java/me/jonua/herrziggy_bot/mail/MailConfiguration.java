package me.jonua.herrziggy_bot.mail;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.model.BaseEntity;

import java.time.ZoneId;

@Getter
@Setter
@Entity
public class MailConfiguration extends BaseEntity {
    private boolean active = false;
    private boolean debug = false;
    private String username;
    private String password;
    private String storeProtocol;
    private String imapsHost;
    private Integer imapsPort;
    private Long imapsTimeout;

    private ZoneId zoneId;
    private String forwardToTelegramGroupId;
}
