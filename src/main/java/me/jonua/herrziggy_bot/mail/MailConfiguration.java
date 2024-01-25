package me.jonua.herrziggy_bot.mail;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.model.BaseEntity;
import me.jonua.herrziggy_bot.model.TgSource;

import java.time.ZoneId;
import java.util.Date;

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

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUse;

    @ManyToOne
    private TgSource tgSource;
}
