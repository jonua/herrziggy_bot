package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventItemDto;
import me.jonua.herrziggy_bot.calendar.dto.CalendarEventsDto;
import me.jonua.herrziggy_bot.utils.DateTimeUtils;
import me.jonua.herrziggy_bot.utils.RetrofitUtils;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarAdapter {
    private final GoogleCalendarApi googleCalendarApi;

    @Value("${bot.chat-id}")
    private String chatId;

    public void proceedUpdate(AbsSender sender, Update update) {
        if (update.getMessage().isCommand()) {
            for (MessageEntity entity : update.getMessage().getEntities()) {
                if (entity.getType().equalsIgnoreCase("bot_command")) {
                    BotCommandsCalendar command = BotCommandsCalendar.fromString(entity.getText());
                    ZonedDateTime now = ZonedDateTime.now();
                    handleCommand(sender, command, now);
                }
            }
        }
    }

    private void handleCommand(AbsSender sender, BotCommandsCalendar command, ZonedDateTime now) {
        switch (command) {
            case TWO_DAYS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfNextDay(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, command, timeMin, timeMax, null);
            }
            case THIS_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, command, timeMin, timeMax, null);
            }
            case NEXT_WEEK -> {
                String timeMin = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now), DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getLastDateTimeOfWeek(now).plusWeeks(1), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, command, timeMin, timeMax, null);
            }
            case CURRENT_30_DAYS_SEMINARS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, command, timeMin, timeMax, "семинар");
            }
            case CURRENT_30_DAYS_TESTS -> {
                String timeMin = DateTimeUtils.formatDate(now, DateTimeUtils.FORMAT_FULL);
                String timeMax = DateTimeUtils.formatDate(DateTimeUtils.getEndOfDay(now.plusDays(60)), DateTimeUtils.FORMAT_FULL);
                respondWithCalendarData(sender, command, timeMin, timeMax, "зачет");
            }
            default -> {
                throw new RuntimeException("Unknown command: " + command.getCommand());
            }
        }
    }

    private void respondWithCalendarData(AbsSender sender, BotCommandsCalendar command, String timeMin, String timeMax, String q) {
        CalendarEventsDto events = RetrofitUtils.executeWithResult(googleCalendarApi.searchEvents(timeMin, timeMax, q));
        List<String> lines = new ArrayList<>();

        for (CalendarEventItemDto item : events.getItems()) {
            String line = String.format("__%s__ _%s\\-%s_: *%s*",
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getStart().getDateTime(), DateTimeUtils.FORMAT_SHORT_DATE_WITH_DAY_NAME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getStart().getDateTime(), DateTimeUtils.FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, DateTimeUtils.formatDate(item.getEnd().getDateTime(), DateTimeUtils.FORMAT_SHORT_TIME)),
                    TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, item.getSummary())
            );
            lines.add(line);
        }

        SendMessage sendMessage;
        if (lines.isEmpty()) {
            sendMessage = buildNoCalendarData(lines);
        } else {
            sendMessage = buildCalendarData(command, lines);
        }

        try {
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage buildNoCalendarData(List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2, "Ничего не нашлось:)");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(tgMessage);
    }

    @NotNull
    private SendMessage buildCalendarData(BotCommandsCalendar command, List<String> lines) {
        String tgMessage = TelegramMessageUtils.tgEscape(ParseMode.MARKDOWNV2,  command.getCommandDescription() + ":\n\n");
        tgMessage += String.join("\n\n", lines);

        return buildTelegramSendMessage(tgMessage);
    }

    @NotNull
    private SendMessage buildTelegramSendMessage(String tgMessage) {
        return new SendMessage(
                chatId,
                null,
                tgMessage,
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
