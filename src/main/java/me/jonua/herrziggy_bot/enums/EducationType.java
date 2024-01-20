package me.jonua.herrziggy_bot.enums;

import lombok.Getter;

@Getter
public enum EducationType {
    MASTER_S("Магистратура"),
    SECOND("Второе высшее"),
    ADDITIONAL("Дополнительное"),
    ;

    private final String description;

    EducationType(String description) {
        this.description = description;
    }
}
