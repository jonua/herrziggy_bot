package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconfigureCalendarCommandHandler implements CommandHandler {
    @Value("${bot.calendar.select-your-calendar-message}")
    private String selectYourCalendarMessage;
    private final MessageSender messageSender;
    private final MessageHandlerService messageHandlerService;
    private final StorageService storageService;

    @Override
    public void handleCommand(BotCommand command, User from, Update update) {
        List<InlineKeyboardButton> buttons = storageService.getCalendars().stream()
                .map(this::buildCalendarButton)
                .toList();

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(List.of(buttons))
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
            messageHandlerService.waitUserFlow(update.getMessage().getFrom(), UserFlowType.RECEIVE_NEW_CALENDAR_CONFIG);
        } catch (TelegramApiException e) {
            log.error("Unable to send request to reconfigure a calendar: {}", e.getMessage(), e);
            messageHandlerService.stopWaiting(update.getMessage().getFrom().getId());
        }
    }

    private InlineKeyboardButton buildCalendarButton(Calendar calendar) {
        return InlineKeyboardButton.builder().callbackData("calendar:" + calendar.getUuid()).text(buildCalendarName(calendar)).build();
    }

    private String buildCalendarName(Calendar calendar) {
        return calendar.getEducationTypeDescription() + ", " + calendar.getParticipationTypeDescription() + ", " +
                "год поступления " + calendar.getEnteringYear();
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommandType.CALENDAR_SETTINGS.equals(command.getCommandType());
    }
}
