package me.jonua.herrziggy_bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tg_source_calendar",
            joinColumns = @JoinColumn(name = "tg_source_id"),
            inverseJoinColumns = @JoinColumn(name = "calendar_id")
    )
    private List<Calendar> calendars = new ArrayList<>();
}
