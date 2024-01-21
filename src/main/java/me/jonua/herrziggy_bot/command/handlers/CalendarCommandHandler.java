package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.calendar.GoogleCalendarApi;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.service.StorageService;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static me.jonua.herrziggy_bot.utils.DateTimeUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarCommandHandler implements CommandHandler {
    @Value("${default-zone-id}")
    private ZoneId zoneId;

    private final GoogleCalendarApi googleCalendarApi;
    private final MessageSender messageSender;
    private final StorageService storageService;
    private final CommandHandlerService commandHandlerService;

    @Override
    public void handleCommand(BotCommand command, User from, Update update) {
        String tgUserId = String.valueOf(update.getMessage().getFrom().getId());
        Optional<Calendar> calendarOpt = storageService.findCalendarByUser(tgUserId);
        calendarOpt.ifPresentOrElse(calendar -> {
            sendCalendarTo(calendar.getGoogleCalendarId(), tgUserId, command);
        }, () -> {
            log.warn("No calendar found for user:{}", tgUserId);
            commandHandlerService.handleCommand(BotCommand.RECONFIGURE_CALENDAR, from, update);
        });
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return Arrays.stream(BotCommand.values())
                .anyMatch(cmd -> BotCommandType.CALENDAR.equals(command.getCommandType()));
    }

    private void sendCalendarTo(String calendarId, String tgUserId, BotCommand command) {
        ZonedDateTime now = ZonedDateTime.now();
        switch (command) {
            case TWO_DAYS -> {
                String timeMin = formatDate(getStartOfDay(now), FORMAT_FULL);
                String timeMax = formatDate(getEndOfNextDay(now), FORMAT_FULL);
                respondWithCalendarData(calendarId, tgUserId, command, timeMin, timeMax, null);
            }
            case THIS_WEEK -> {
                String timeMin = formatDate(getStartOfDay(now), FORMAT_FULL);
                String timeMax = formatDate(getLastDateTimeOfWeek(now), FORMAT_FULL);
                respondWithCalendarData(calendarId, tgUserId, command, timeMin, timeMax, null);
            }
            case NEXT_WEEK -> {
                String timeMin = formatDate(getLastDateTimeOfWeek(now), FORMAT_FULL);
                String timeMax = formatDate(getLastDateTimeOfWeek(now).plusWeeks(1), FORMAT_FULL);
                respondWithCalendarData(calendarId, tgUserId, command, timeMin, timeMax, null);
            }
            case CURRENT_30_DAYS_SEMINARS -> {
                String timeMin = formatDate(getStartOfDay(now), FORMAT_FULL);
                String timeMax = formatDate(getEndOfDay(now.plusDays(60)), FORMAT_FULL);
                respondWithCalendarData(calendarId, tgUserId, command, timeMin, timeMax, "семинар");
            }
            case CURRENT_30_DAYS_TESTS -> {
                String timeMin = formatDate(getStartOfDay(now), FORMAT_FULL);
                String timeMax = formatDate(getEndOfDay(now.plusDays(60)), FORMAT_FULL);
                respondWithCalendarData(calendarId, tgUserId, command, timeMin, timeMax, "зачет");
            }
            default -> {
                log.error("Unsupported calendar command: {}", command.getCommand());
                throw new RuntimeException("Unsupported command: " + command.getCommand());
            }
        }
    }

    private void respondWithCalendarData(String calendarId, String tgUserId, BotCommand command, String timeMin, String timeMax, String q) {
        log.trace("Command will be executed on a calendar: {} with start date:{}, end date:{} and query:{}",
                command.getCommand(), timeMin, timeMax, q);

        CalendarEventsDto events = googleCalendarApi.searchEvents(calendarId, timeMin, timeMax, q);
        List<String> eventList = new ArrayList<>();

        for (CalendarEventItemDto item : events.getItems()) {
            String line = String.format("__%s__ _%s\\-%s_: *%s*",
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getStart().getDateTime(), zoneId, FORMAT_SHORT_DATE_WITH_DAY_NAME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getStart().getDateTime(), zoneId, FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, formatDate(item.getEnd().getDateTime(), zoneId, FORMAT_SHORT_TIME)),
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
            sendMessage = buildCalendarData(tgUserId, command, eventList);
        }

        try {
            log.trace("Send calendar events to the telegram conversation: {}", tgUserId);
            messageSender.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to sent calendar events to the telegram conversation:{}: {}", tgUserId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildNoCalendarData(String tgUserId, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, "Ничего не нашлось:)");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(tgUserId, tgMessage);
    }

    @NotNull
    private SendMessage buildCalendarData(String tgUserId, BotCommand command, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, command.getDescription() + ":\n\n");
        tgMessage += String.join("\n\n", lines);

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

    public void sendCalendar(String calendarId, String sendToId, BotCommand command) {
        sendCalendarTo(calendarId, sendToId, command);
    }
}
