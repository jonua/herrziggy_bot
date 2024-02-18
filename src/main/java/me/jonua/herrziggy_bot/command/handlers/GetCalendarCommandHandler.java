package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.enums.calendar.CalendarPeriod;
import me.jonua.herrziggy_bot.service.CalendarService;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.util.*;

import static me.jonua.herrziggy_bot.utils.DateTimeUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCalendarCommandHandler extends BaseCommandHandler {
    @Value("${default-zone-id}")
    private ZoneId zoneId;
    @Value("${bot.locale}")
    private Locale locale;
    @Value("${messages.calendar-no-events-found-message}")
    private String noCalendarEventsFound;
    private final MessageSender messageSender;
    private final CommandHandlerService commandHandlerService;
    private final CalendarService calendarService;

    public void handleCommand(BotCommand command, User from, Update update) {
        handleCommand(command, from, update, Map.of());
    }

    @Override
    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        String tgUserId = String.valueOf(update.getMessage().getFrom().getId());
        List<String> googleCalendarIds = calendarService.findGoogleCalendarIdsByUser(tgUserId);

        boolean respondWithNoDataMessage = (boolean) payload.getOrDefault("respondWithNoDataMessage", false);
        if (googleCalendarIds.isEmpty()) {
            log.warn("No calendar found for user:{}", tgUserId);
            commandHandlerService.handleCommand(BotCommand.SETUP_USER_CALENDAR, from, update);
        } else {
            String calendarName = calendarService.getCalendarNameByUser(tgUserId);
            sendCalendar(calendarName, googleCalendarIds, tgUserId, command, respondWithNoDataMessage);
        }
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return Arrays.stream(BotCommand.values())
                .anyMatch(cmd -> BotCommandType.CALENDAR.equals(command.getCommandType()));
    }

    private void sendCalendarTo(String calendarName, List<String> calendarIds, String tgUserId, BotCommand command, boolean respondWithNoDataMessage) {
        switch (command) {
            case TWO_DAYS -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.TWO_DAYS, calendarIds);
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case THIS_WEEK -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.THIS_WEEK, calendarIds);
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case NEXT_WEEK -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.NEXT_WEEK, calendarIds);
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case CURRENT_30_DAYS_SEMINARS -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.CURRENT_30_DAYS_SEMINARS, calendarIds, "семинар");
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case CURRENT_30_DAYS_TESTS -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.CURRENT_30_DAYS_TESTS, calendarIds, "зачет");
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case FULL_SEMESTER_SEMINARS -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.FULL_SEMESTER_SEMINARS, calendarIds, "семинар");
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }
            case FULL_SEMESTER_TESTS -> {
                List<CalendarEventItemDto> items = calendarService.getMergedCalendarsEvents(CalendarPeriod.FULL_SEMESTER_TESTS, calendarIds, "зачет");
                respondWithCalendarData(calendarName, tgUserId, command, items, respondWithNoDataMessage);
            }

            default -> {
                log.error("Unsupported calendar command: {}", command.getCommand());
                throw new RuntimeException("Unsupported command: " + command.getCommand());
            }
        }
    }

    private void respondWithCalendarData(String calendarName, String tgUserId, BotCommand command, List<CalendarEventItemDto> items, boolean respondWithNoDataMessage) {
        List<String> eventList = new ArrayList<>();

        for (CalendarEventItemDto item : items) {
            String line = String.format("__%s__ _%s\\-%s_: *%s*",
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getStart().getDateTime(), zoneId, locale, FORMAT_SHORT_DATE_WITH_DAY_NAME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getStart().getDateTime(), zoneId, locale, FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getEnd().getDateTime(), zoneId, locale, FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, item.getSummary())
            );
            eventList.add(line);
        }

        SendMessage sendMessage;
        if (eventList.isEmpty()) {
            log.trace("No calendar events found");
            sendMessage = buildNoCalendarData(tgUserId, eventList);
        } else {
            log.trace("Found {} calendar events", eventList.size());
            sendMessage = buildCalendarData(calendarName, tgUserId, command, eventList);
        }

        sendCalendarDataToUser(tgUserId, sendMessage, respondWithNoDataMessage);
    }

    private void sendCalendarDataToUser(String tgUserId, SendMessage sendMessage, boolean respondWithNoDataMessage) {
        try {
            if (respondWithNoDataMessage) {
                log.trace("Send calendar events to the telegram conversation: {}", tgUserId);
                messageSender.send(sendMessage);
            } else {
                log.warn("No calendar events found and users will not be notified");
            }
        } catch (TelegramApiException e) {
            log.error("Unable to sent calendar events to the telegram conversation:{}: {}", tgUserId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildNoCalendarData(String tgUserId, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, noCalendarEventsFound);
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(tgUserId, tgMessage);
    }

    @NotNull
    private SendMessage buildCalendarData(String calendarName, String tgUserId, BotCommand command, List<String> events) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, command.getDescription() + " (" + calendarName + "):\n\n");
        tgMessage += String.join("\n\n", events);

        return buildTelegramSendMessage(tgUserId, tgMessage);
    }

    @NotNull
    private SendMessage buildTelegramSendMessage(String tgUserId, String tgMessage) {
        String reducedMessage = TelegramMessageUtils.reduceMessageIfNeeds(ParseMode.MARKDOWNV2, tgMessage);
        return new SendMessage(
                tgUserId,
                null,
                reducedMessage,
                ParseMode.MARKDOWNV2,
                false,
                false,
                null,
                null,
                null,
                true,
                false
        );
    }

    public void sendCalendar(String calendarName, List<String> calendarIds, String sendToId, BotCommand command, boolean respondWithNoDataMessage) {
        sendCalendarTo(calendarName, calendarIds, sendToId, command, respondWithNoDataMessage);
    }
}
