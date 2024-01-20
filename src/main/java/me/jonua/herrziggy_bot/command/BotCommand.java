package me.jonua.herrziggy_bot.command;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum BotCommand {
    TWO_DAYS(BotCommandType.CALENDAR, "two_days", "Календарь на ближайшие 2 дня (включая сегодняшний)", true),
    THIS_WEEK(BotCommandType.CALENDAR, "this_week", "Календарь на эту неделю", true),
    NEXT_WEEK(BotCommandType.CALENDAR, "next_week", "Календарь на следующую неделю", true),
    CURRENT_30_DAYS_SEMINARS(BotCommandType.CALENDAR, "30days_seminars", "Семинары на ближайшие 30 дней", true),
    CURRENT_30_DAYS_TESTS(BotCommandType.CALENDAR, "30days_tests", "Зачеты на ближайшие 30 дней", true),
    RECONFIGURE_CALENDAR(BotCommandType.CALENDAR_SETTINGS, "reconfigure_calendar", "Поменять календарь", true),
    FEEDBACK(BotCommandType.FEEDBACK, "feedback", "Обратная связь", true),
    CANCEL_FEEDBACK(BotCommandType.SERVICE, "cancel_feedback", "Отменить фидбек", false);

    private final BotCommandType commandType;
    private final String command;

    private final String description;
    private final boolean includeInBotMenu;

    BotCommand(BotCommandType commandType, String command, String description, boolean addToBotMenu) {
        this.commandType = commandType;
        this.command = command;
        this.description = description;
        this.includeInBotMenu = addToBotMenu;
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
