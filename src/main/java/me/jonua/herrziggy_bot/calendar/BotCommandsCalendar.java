package me.jonua.herrziggy_bot.calendar;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum BotCommandsCalendar {
    TWO_DAYS("two_days", "Календарь на ближайшие 2 дня (включая сегодняшний)"), // - Календарь на 2 ближайшие дня (включая сегодня)
    THIS_WEEK("this_week", "Календарь на эту неделю"), // - Календарь на текущую неделю
    NEXT_WEEK("next_week", "Календарь на следующую неделю"), // - Календарь на следующую неделею
    CURRENT_30_DAYS_SEMINARS("30days_seminars", "Семинары на ближайшие 30 дней"), // - Только семинары в ближайшие 30 дней
    CURRENT_30_DAYS_TESTS("30days_tests", "Зачеты на ближайшие 30 дней"); // - Только семинары в ближайшие 30 дней

    private final String command;

    private final String commandDescription;

    BotCommandsCalendar(String command, String description) {
        this.command = command;
        this.commandDescription = description;
    }

    public static BotCommandsCalendar fromString(String text) {
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("Empty command");

        }
        String rawCommand = text.split("@")[0].substring(1);
        for (BotCommandsCalendar botCommand : BotCommandsCalendar.values()) {
            if (botCommand.getCommand().equalsIgnoreCase(rawCommand)) {
                return botCommand;
            }
        }

        throw new RuntimeException("Unknown command: " + rawCommand);
    }
}
