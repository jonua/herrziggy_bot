package me.jonua.herrziggy_bot.command;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum BotCommand {
    TWO_DAYS(CommandType.CALENDAR, "two_days", "Календарь на ближайшие 2 дня (включая сегодняшний)"), // - Календарь на 2 ближайшие дня (включая сегодня)
    THIS_WEEK(CommandType.CALENDAR, "this_week", "Календарь на эту неделю"), // - Календарь на текущую неделю
    NEXT_WEEK(CommandType.CALENDAR, "next_week", "Календарь на следующую неделю"), // - Календарь на следующую неделею
    CURRENT_30_DAYS_SEMINARS(CommandType.CALENDAR, "30days_seminars", "Семинары на ближайшие 30 дней"), // - Только семинары в ближайшие 30 дней
    CURRENT_30_DAYS_TESTS(CommandType.CALENDAR, "30days_tests", "Зачеты на ближайшие 30 дней"), // - Только семинары в ближайшие 30 дней
    FEEDBACK(CommandType.FEEDBACK, "feedback", "Обратная связь"), // - Только семинары в ближайшие 30 дней
    CANCEL_FEEDBACK(CommandType.SERVICE, "cancel_feedback", "Отменить фидбек"); // - Только семинары в ближайшие 30 дней

    private final CommandType commandType;
    private final String command;

    private final String description;

    BotCommand(CommandType commandType, String command, String description) {
        this.commandType = commandType;
        this.command = command;
        this.description = description;
    }

    public static BotCommand fromString(String text) {
        if (StringUtils.isEmpty(text)) {
            throw new RuntimeException("Empty command");

        }
        String rawCommand = text.split("@")[0].substring(1);
        for (BotCommand botCommand : BotCommand.values()) {
            if (botCommand.getCommand().equalsIgnoreCase(rawCommand)) {
                return botCommand;
            }
        }

        throw new RuntimeException("Unknown command: " + rawCommand);
    }
}
