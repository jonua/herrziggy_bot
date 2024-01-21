package me.jonua.herrziggy_bot.enums.calendar;

import lombok.Getter;

@Getter
public enum CalendarPeriod {
    TWO_DAYS("Календарь на ближайшие 2 дня (включая сегодня)"),
    THIS_WEEK("Календарь на эту неделю"),
    NEXT_WEEK("Календарь на следующую неделю"),
    CURRENT_30_DAYS_SEMINARS("Семинары на ближайшие 30 дней"),
    CURRENT_30_DAYS_TESTS("Зачеты на ближайшие 30 дней"),
    ;

    private final String description;

    CalendarPeriod(String description) {
        this.description = description;
    }
}
