package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.service.StorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetUpUserCalendarCommandHandler extends BaseCommandHandler {
    @Value("${bot.calendar.select-your-calendar-message}")
    private String selectYourCalendarMessage;
    private final MessageSender messageSender;
    private final MessageHandlerService messageHandlerService;
    private final StorageService storageService;

    @Override
    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        List<List<InlineKeyboardButton>> buttons = buildButtons();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();

        SendMessage tgMessage = new SendMessage(
                String.valueOf(update.getMessage().getFrom().getId()),
                null,
                selectYourCalendarMessage,
                null,
                false,
                false,
                null,
                keyboard,
                null,
                true,
                false
        );

        try {
            messageSender.send(tgMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to send request to reconfigure a calendar: {}", e.getMessage(), e);
            messageHandlerService.stopWaiting(update.getMessage().getFrom().getId());
        }
    }

    @NotNull
    private List<List<InlineKeyboardButton>> buildButtons() {
        Sort sorting = Sort.by(Sort.Order.asc("orderValue"));
        List<CalendarConfiguration> calendars = storageService.getCalendars(sorting);

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (CalendarConfiguration calendar : calendars) {
            InlineKeyboardButton calendarButton = buildCalendarButton(calendar);
            if (buttons.isEmpty() || buttons.getLast().size() % 2 == 0) {
                buttons.add(new ArrayList<>());
            }

            buttons.getLast().add(calendarButton);
        }

        return buttons;
    }

    private InlineKeyboardButton buildCalendarButton(CalendarConfiguration calendar) {
        return InlineKeyboardButton.builder()
                .callbackData("cf:calendar:" + calendar.getUuid())
                .text(buildCalendarName(calendar))
                .build();
    }

    private String buildCalendarName(CalendarConfiguration calendar) {
        return calendar.getAdditionalInfo();
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommandType.CALENDAR_SETTINGS.equals(command.getCommandType());
    }
}
