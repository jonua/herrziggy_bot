package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.CommandHandler;
import me.jonua.herrziggy_bot.utils.DateTimeUtils;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarCommandHandler implements CommandHandler {
    @Value("${default-zone-id}")
    private ZoneId zoneId;

    private final GoogleCalendarApi googleCalendarApi;
    private final MessageSender messageSender;

    @Override
    public void handleCommand(Message message, BotCommand command) {
        User fromUser = message.getFrom();
        sendCalendarTo(fromUser, command);
    }

    private void sendCalendarTo(User sendTo, BotCommand command) {
        ZonedDateTime now = ZonedDateTime.now();
        switch (command) {
            case TWO_DAYS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfNextDay(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sendTo, command, timeMin, timeMax, null);
            }
            case THIS_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sendTo, command, timeMin, timeMax, null);
            }
            case NEXT_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now).plusWeeks(1), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sendTo, command, timeMin, timeMax, null);
            }
            case CURRENT_30_DAYS_SEMINARS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sendTo, command, timeMin, timeMax, "семинар");
            }
            case CURRENT_30_DAYS_TESTS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sendTo, command, timeMin, timeMax, "зачет");
            }
            default -> {
                log.error("Unsupported calendar command: {}", command.getCommand());
                throw new RuntimeException("Unsupported command: " + command.getCommand());
            }
        }
    }

    private void respondWithCalendarData(User fromUser, BotCommand command, String timeMin, String timeMax, String q) {
        log.trace("Command will be executed on a calendar: {} with start date:{}, end date:{} and query:{}",
                command.getCommand(), timeMin, timeMax, q);
        CalendarEventsDto events = googleCalendarApi.searchEvents(timeMin, timeMax, q);
        List<String> eventList = new ArrayList<>();

        for (CalendarEventItemDto item : events.getItems()) {
            String line = String.format("__%s__ _%s\\-%s_: *%s*",
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getStart().getDateTime(), zoneId, DateTimeUtils.FORMAT_SHORT_DATE_WITH_DAY_NAME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getStart().getDateTime(), zoneId, DateTimeUtils.FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getEnd().getDateTime(), zoneId, DateTimeUtils.FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, item.getSummary())
            );
            eventList.add(line);
        }

        SendMessage sendMessage;
        if (eventList.isEmpty()) {
            log.trace("No calendar events found");
            sendMessage = buildNoCalendarData(fromUser, eventList);
        } else {
            log.trace("Found {} calendar events", eventList.size());
            sendMessage = buildCalendarData(fromUser, command, eventList);
        }

        try {
            log.trace("Send calendar events to the telegram conversation: {}", fromUser);
            messageSender.send(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to sent calendar events to the telegram conversation:{}: {}", fromUser, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildNoCalendarData(User fromUser, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, "Ничего не нашлось:)");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(fromUser, tgMessage);
    }

    @NotNull
    private SendMessage buildCalendarData(User fromUser, BotCommand command, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2,  command.getDescription() + ":\n\n");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(fromUser, tgMessage);
    }

    @NotNull
    private SendMessage buildTelegramSendMessage(User fromUser, String tgMessage) {
        String reducedMessage = TelegramMessageUtils.reduceMessageIfNeeds(ParseMode.MARKDOWNV2, tgMessage);
        return new SendMessage(
                String.valueOf(fromUser.getId()),
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

    public void sendCalendar(long sendToId, BotCommand command) {
        sendCalendarTo(new User(sendToId, "", false), command);
    }
}
