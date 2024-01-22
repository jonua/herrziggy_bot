package me.jonua.herrziggy_bot.command.handlers;

import me.jonua.herrziggy_bot.command.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

public interface CommandHandler {
    void handleCommand(BotCommand command, User from, Update update);

    void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload);

    abstract boolean isSupport(BotCommand command);
}
