package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledGroupNotifier {
    @Value("${bot.chat-id}")
    private String groupId;

    private final CalendarAdapter calendarAdapter;
    private final AbsSender absSender;

    @Scheduled(cron = "0 0 7 * * 1") // every monday at 7 o'clock by UTC
    private void notifyAlAbotCalendarEvents() {
        log.info("Notifying group {} about next week events...", groupId);
        calendarAdapter.handleCommand(absSender, groupId, BotCommandsCalendar.THIS_WEEK, ZonedDateTime.now());
    }
}
