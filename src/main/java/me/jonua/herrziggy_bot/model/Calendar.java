package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.enums.EducationParticipationType;
import me.jonua.herrziggy_bot.enums.EducationType;

@Getter
@Setter
@Entity
public class Calendar extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private EducationType educationType;
    private String educationTypeDescription;
    @Enumerated(EnumType.STRING)
    private EducationParticipationType participationType;
    private String participationTypeDescription;
    private Integer enteringYear;
    private String googleCalendarId;
}
