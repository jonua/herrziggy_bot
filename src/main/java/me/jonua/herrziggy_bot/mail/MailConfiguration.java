package me.jonua.herrziggy_bot.mail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.model.BaseEntity;
import me.jonua.herrziggy_bot.model.TgSource;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "mail_subscriber",
            inverseJoinColumns = @JoinColumn(name = "tg_source_id"),
            joinColumns = @JoinColumn(name = "mail_configuration_id")
    )
    private List<TgSource> tgSources;
}
