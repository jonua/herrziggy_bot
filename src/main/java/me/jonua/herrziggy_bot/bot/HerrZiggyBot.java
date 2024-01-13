package me.jonua.herrziggy_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class HerrZiggyBot extends TelegramLongPollingBot {
    private static final String BOT_NAME = "HerrZiggy_bot";

    public HerrZiggyBot(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Updated received {}", update);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}
