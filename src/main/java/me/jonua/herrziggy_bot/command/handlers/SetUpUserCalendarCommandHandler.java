package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.service.StorageService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

import static me.jonua.herrziggy_bot.utils.TelegramMessageUtils.KeyboardButton;
import static me.jonua.herrziggy_bot.utils.TelegramMessageUtils.buildInlineKeyboardMarkup;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetUpUserCalendarCommandHandler extends BaseCommandHandler {
    @Value("${messages.calendar-select-message}")
    private String selectYourCalendarMessage;
    private final MessageSender messageSender;
    private final MessageHandlerService messageHandlerService;
    private final StorageService storageService;

    @Override
    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        InlineKeyboardMarkup keyboard = buildKeyboard();
        try {
            messageSender.send(selectYourCalendarMessage, keyboard, update.getMessage().getFrom().getId());
        } catch (TelegramApiException e) {
            log.error("Unable to send request to reconfigure a calendar: {}", e.getMessage(), e);
            messageHandlerService.stopWaiting(update.getMessage().getFrom().getId());
        }
    }

    @NotNull
    private InlineKeyboardMarkup buildKeyboard() {
        Sort sorting = Sort.by(Sort.Order.asc("orderValue"));
        List<CalendarConfiguration> calendars = storageService.getCalendars(sorting);

        List<KeyboardButton> keyboards = calendars.stream()
                .map(calendar -> {
                    String buttonName = calendar.getAdditionalInfo();
                    String buttonCallbackData = UserFlowType.RECEIVE_NEW_CALENDAR_CONFIG.getCommandPrefix() + ":calendar:" + calendar.getUuid();
                    return new KeyboardButton(buttonName, buttonCallbackData);
                })
                .toList();

        return buildInlineKeyboardMarkup(keyboards, 2);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommandType.CALENDAR_SETTINGS.equals(command.getCommandType());
    }
}
