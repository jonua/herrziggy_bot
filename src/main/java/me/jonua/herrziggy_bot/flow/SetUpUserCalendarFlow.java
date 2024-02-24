package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetUpUserCalendarFlow implements UserFlow {
    @Value("${messages.calendar-reconfigured-message}")
    private String calendarReconfiguredMessage;
    private final StorageService storageService;
    private final MessageSender messageSender;

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        String newCalendarUuid = ((List<String>)params.get(UserFlow.PARAM_CALLBACK_DATA)).get(2);
        storageService.findCalendarByUuid(newCalendarUuid)
                .ifPresentOrElse(calendar -> reassignCalendar(update, calendar), () -> {
                    log.error("No calendars found by uuid {}", newCalendarUuid);
                });
    }

    private void reassignCalendar(Update update, CalendarConfiguration calendar) {
        User from = update.getCallbackQuery().getFrom();
        storageService.assignCalendar(from.getId(), calendar.getUuid());
        AnswerCallbackQuery answerQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(calendarReconfiguredMessage)
                .build();
        try {
            messageSender.send(answerQuery);
            messageSender.sendSilently(calendarReconfiguredMessage, from.getId());
        } catch (TelegramApiException e) {
            log.error("Unable to send message about a calendar has been reconfigured: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.RECEIVE_NEW_CALENDAR_CONFIG.equals(userFlowType);
    }
}
