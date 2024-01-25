package me.jonua.herrziggy_bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class HerrZiggyBot extends TelegramLongPollingBot {
    private static final String BOT_NAME = "HerrZiggy_bot";
    private final TgUpdateHandler tgUpdateHandler;

    public HerrZiggyBot(DefaultBotOptions options, String botToken, TgUpdateHandler tgUpdateHandler) throws TelegramApiException {
        super(options, botToken);
        this.tgUpdateHandler = tgUpdateHandler;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.trace("Updated received {}", update);
        tgUpdateHandler.handleUpdate(update);
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
}
