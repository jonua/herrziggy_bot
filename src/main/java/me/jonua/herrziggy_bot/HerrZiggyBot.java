package me.jonua.herrziggy_bot;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.BotCommandsCalendar;
import me.jonua.herrziggy_bot.calendar.CalendarAdapter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class HerrZiggyBot extends TelegramLongPollingBot {
    private static final String BOT_NAME = "HerrZiggy_bot";
    private final CalendarAdapter calendarAdapter;

    public HerrZiggyBot(DefaultBotOptions options, String botToken, CalendarAdapter calendarAdapter) throws TelegramApiException {
        super(options, botToken);
        this.calendarAdapter = calendarAdapter;
        initializeCommands();
    }

    private void initializeCommands() throws TelegramApiException {
        List<? extends BotCommand> botCommands = Arrays.stream(BotCommandsCalendar.values())
                .map(botCommand -> BotCommand.builder().command(botCommand.getCommand()).description(botCommand.getDescription()).build())
                .toList();

        execute(SetMyCommands.builder()
                .commands(botCommands)
                .scope(BotCommandScopeAllPrivateChats.builder().build()).build());
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Updated received {}", update);
        calendarAdapter.proceedUpdate(this, update);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}
