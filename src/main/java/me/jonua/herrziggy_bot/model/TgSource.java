package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TgSource extends BaseEntity {
    private String sourceId;
    private String type;
    private String firstName;
    private String lastName;
    private String username;
    private String title;
    private Boolean isBot = false;
    private Boolean isPremium = false;
    private String migrateFromChatId;

    @ManyToOne
    private CalendarConfiguration calendarConfiguration;
}
