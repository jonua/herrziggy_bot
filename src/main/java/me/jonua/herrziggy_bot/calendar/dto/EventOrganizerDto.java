package me.jonua.herrziggy_bot.calendar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventOrganizerDto extends EventPersonDto {
    private String displayName;
    private Boolean self;
}
