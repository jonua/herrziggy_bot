package me.jonua.herrziggy_bot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.enums.EducationParticipationType;
import me.jonua.herrziggy_bot.enums.EducationType;

import java.util.List;

@Getter
@Setter
@Entity
public class CalendarConfiguration extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private EducationType educationType;
    private String educationTypeDescription;
    @Enumerated(EnumType.STRING)
    private EducationParticipationType participationType;
    private String participationTypeDescription;
    private String additionalInfo;
    private Integer enteringYear;
    private Integer orderValue;

    private boolean mergeCalendars = true;

    @ManyToMany
    @JoinTable(
            name = "calendar_configuration_source",
            joinColumns = @JoinColumn(name = "calendar_configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "calendar_source_id")
    )
    private List<CalendarSource> calendarSources;
}
