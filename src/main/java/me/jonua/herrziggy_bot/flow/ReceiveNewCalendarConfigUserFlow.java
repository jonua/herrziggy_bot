package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveNewCalendarConfigUserFlow implements UserFlow {
    @Value("${bot.calendar.reconfigured-message}")
    private String calendarReconfiguredMessage;
    private final MessageHandlerService messageHandlerService;
    private final StorageService storageService;
    private final MessageSender messageSender;

    @Override
    public void perform(Update update) {
        messageHandlerService.stopWaiting(update.getCallbackQuery().getFrom().getId());

        String callbackData = update.getCallbackQuery().getData();
        String[] config = callbackData.split(":");
        String newCalendarUuid = config[1];
        storageService.findCalendarByUuid(newCalendarUuid)
                .ifPresentOrElse(calendar -> reassignCalendar(update, calendar), () -> {
                    log.error("No calendars found by uuid {}", newCalendarUuid);
                });
    }

    private void reassignCalendar(Update update, Calendar calendar) {
        User from = update.getCallbackQuery().getFrom();
        storageService.assignCalendar(from.getId(), calendar.getUuid());
        AnswerCallbackQuery answerQuery = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .text(calendarReconfiguredMessage)
                .build();
        try {
            messageSender.send(answerQuery);
            messageSender.send(calendarReconfiguredMessage, from.getId());
        } catch (TelegramApiException e) {
            log.error("Unable to send message about a calendar has been reconfigured: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.RECEIVE_NEW_CALENDAR_CONFIG.equals(userFlowType);
    }
}
