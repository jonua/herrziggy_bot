package me.jonua.herrziggy_bot;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.CalendarAdapter;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class HerrZiggyBot extends TelegramLongPollingBot {
    private static final String BOT_NAME = "HerrZiggy_bot";
    private final CalendarAdapter calendarAdapter;

    public HerrZiggyBot(DefaultBotOptions options, String botToken, CalendarAdapter calendarAdapter) {
        super(options, botToken);
        this.calendarAdapter = calendarAdapter;
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
