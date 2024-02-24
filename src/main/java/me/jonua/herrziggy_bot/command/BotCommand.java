package me.jonua.herrziggy_bot.command;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum BotCommand {
    TWO_DAYS(BotCommandType.CALENDAR, "two_days", "\uD83D\uDDD3 Ближайшие 2 дня (включая сегодня)", true),
    THIS_WEEK(BotCommandType.CALENDAR, "this_week", "\uD83D\uDDD3 Эта неделя", true),
    NEXT_WEEK(BotCommandType.CALENDAR, "next_week", "\uD83D\uDDD3 Следующая неделя", true),
    CURRENT_30_DAYS_SEMINARS(BotCommandType.CALENDAR, "30days_seminars", "\uD83D\uDCC5 Семинары на ближайшие 30 дней", true),
    CURRENT_30_DAYS_TESTS(BotCommandType.CALENDAR, "30days_tests", "\uD83D\uDCC6 Зачеты на ближайшие 30 дней", true),
    FULL_SEMESTER_SEMINARS(BotCommandType.CALENDAR, "semesters_seminars", "\uD83D\uDCC5 Семинары на весь семестр", true),
    FULL_SEMESTER_TESTS(BotCommandType.CALENDAR, "semester_tests", "\uD83D\uDCC6 Зачеты на весь семестр", true),
    SETUP_USER_CALENDAR(BotCommandType.CALENDAR_SETTINGS, "setup_calendar", "\uD83E\uDD13 Поменять календарь", true),
    FEEDBACK(BotCommandType.FEEDBACK, "feedback", "Напиши мне \uD83E\uDD19", true),
    CANCEL(BotCommandType.CANCEL, "cancel", "\uD83C\uDF1A Отменить", false),
    START_BOT(BotCommandType.START_BOT, "start", "Star ", false);

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
