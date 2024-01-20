package me.jonua.herrziggy_bot.enums;

import lombok.Getter;

@Getter
public enum EducationParticipationType {
    FULL_TIME("Очная форма"),
    EVENING("Вечерняя форма"),
    PART_TIME("Очно-заочная форма"),
    ;

    private final String description;

    EducationParticipationType(String description) {
        this.description = description;
    }
}
