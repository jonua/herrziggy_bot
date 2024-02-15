package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.GetCalendarCommandHandler;
import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.model.CalendarNotificationConfiguration;
import me.jonua.herrziggy_bot.model.CalendarSource;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledGroupNotifier {
    private final GetCalendarCommandHandler calendarCommandHandler;
    private final CalendarService calendarService;
    @Lazy
    @Autowired
    private ScheduledGroupNotifier self;

    @Bean
    public ScheduledTaskRegistrar scheduledTaskRegistrar() {
        return new ScheduledTaskRegistrar();
    }

    @Scheduled(cron = "${bot.calendar.scheduling-cron}")
    public void updatedCalendarNotificationSchedules() {
        List<CalendarNotificationConfiguration> activeSchedules = calendarService.findActiveConfigurations();
        for (CalendarNotificationConfiguration activeSchedule : activeSchedules) {
            if (activeSchedule.getCalendarConfiguration() == null) {
                log.warn("Schedule {} has no any linked calendars", activeSchedule.getUuid());
                continue;
            }

            self.notifyCalendarSubscribers(activeSchedule.getUuid());
        }
    }

    @Transactional
    public void notifyCalendarSubscribers(String configUuid) {
        CalendarNotificationConfiguration config = calendarService.getNotificationConfiguration(configUuid);
        TgSource source = config.getTgSource();
        CalendarConfiguration calendarConfiguration = source.getCalendarConfiguration();

        log.info("Notifying group {} ({}) about week events from calendars {} ...",
                source.getSourceId(), source.getTitle(),
                calendarConfiguration.getCalendarSources().stream()
                        .map(c -> String.format("%s (%s)", c.getGoogleCalendarId(), c.getName())).collect(Collectors.joining(", "))
        );

        calendarCommandHandler
                .sendCalendar(
                        calendarConfiguration.getAdditionalInfo(),
                        calendarConfiguration.getCalendarSources().stream().map(CalendarSource::getGoogleCalendarId).collect(Collectors.toList()),
                        source.getSourceId(),
                        BotCommand.THIS_WEEK,
                        false
                );
    }
}
