package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CalendarSource extends BaseEntity {
    private String name;
    private String googleCalendarId;
}
