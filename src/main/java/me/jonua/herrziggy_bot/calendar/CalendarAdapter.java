package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.utils.DateTimeUtils;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarAdapter {
    @Value("${default-zone-id}")
    private ZoneId zoneId;

    private final GoogleCalendarApi googleCalendarApi;

    public void proceedUpdate(AbsSender sender, Update update) {
        if (update.getMessage().isCommand()) {
            String senderId = String.valueOf(update.getMessage().getFrom().getId());

            for (MessageEntity entity : update.getMessage().getEntities()) {
                if (entity.getType().equalsIgnoreCase("bot_command")) {
                    BotCommandsCalendar command = BotCommandsCalendar.fromString(entity.getText());
                    ZonedDateTime now = ZonedDateTime.now();
                    handleCommand(sender, senderId, command, now);
                }
            }
        }
    }

    public void handleCommand(AbsSender sender, String conversationId, BotCommandsCalendar command, ZonedDateTime now) {
        switch (command) {
            case TWO_DAYS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfNextDay(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, conversationId, command, timeMin, timeMax, null);
            }
            case THIS_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, conversationId, command, timeMin, timeMax, null);
            }
            case NEXT_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now).plusWeeks(1), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, conversationId, command, timeMin, timeMax, null);
            }
            case CURRENT_30_DAYS_SEMINARS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, conversationId, command, timeMin, timeMax, "семинар");
            }
            case CURRENT_30_DAYS_TESTS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, conversationId, command, timeMin, timeMax, "зачет");
            }
            default -> {
                log.error("Unsupported calendar command: {}", command.getCommand());
                throw new RuntimeException("Unsupported command: " + command.getCommand());
            }
        }
    }

    private void respondWithCalendarData(AbsSender sender, String conversationId, BotCommandsCalendar command, String timeMin, String timeMax, String q) {
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
            sendMessage = buildNoCalendarData(conversationId, eventList);
        } else {
            log.trace("Found {} calendar events", eventList.size());
            sendMessage = buildCalendarData(conversationId, command, eventList);
        }

        try {
            log.trace("Send calendar events to the telegram conversation: {}", conversationId);
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to sent calendar events to the telegram conversation:{}: {}", conversationId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildNoCalendarData(String conversationId, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, "Ничего не нашлось:)");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(conversationId, tgMessage);
    }

    @NotNull
    private SendMessage buildCalendarData(String conversationId, BotCommandsCalendar command, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2,  command.getDescription() + ":\n\n");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(conversationId, tgMessage);
    }

    @NotNull
    private SendMessage buildTelegramSendMessage(String conversationId, String tgMessage) {
        String reducedMessage = TelegramMessageUtils.reduceMessageIfNeeds(ParseMode.MARKDOWNV2, tgMessage);
        return new SendMessage(
                conversationId,
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
}
