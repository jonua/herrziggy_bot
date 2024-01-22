package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.GetCalendarCommandHandler;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledGroupNotifier {
    @Value("${bot.chat-id}")
    private String sourceId;

    private final GetCalendarCommandHandler calendarCommandHandler;
    private final StorageService storageService;

    @Scheduled(cron = "0 0 10 * * 1") // every monday at 7 o'clock by UTC
    private void notifyAlAboutCalendarEvents() {
        log.info("Notifying group {} about next week events...", sourceId);
        storageService.findCalendarByGroup(sourceId)
                .ifPresent(calendar -> calendarCommandHandler.sendCalendar(calendar.getGoogleCalendarId(), sourceId, BotCommand.THIS_WEEK));
    }
}
