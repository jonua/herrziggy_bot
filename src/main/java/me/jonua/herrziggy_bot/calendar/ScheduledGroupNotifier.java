package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledGroupNotifier {
    @Value("${bot.chat-id}")
    private String groupId;

    private final CalendarCommandHandler calendarCommandHandler;

    @Scheduled(cron = "0 0 7 * * 1") // every monday at 7 o'clock by UTC
    private void notifyAlAboutCalendarEvents() {
        log.info("Notifying group {} about next week events...", groupId);
        calendarCommandHandler.sendCalendar(Long.parseLong(groupId), BotCommand.THIS_WEEK);
    }
}
