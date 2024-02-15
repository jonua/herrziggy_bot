package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CalendarNotificationConfiguration extends BaseEntity {
    private boolean active = false;
    @OneToOne(fetch = FetchType.EAGER)
    private CalendarConfiguration calendarConfiguration;
    @OneToOne(fetch = FetchType.EAGER)
    private TgSource tgSource;
}
