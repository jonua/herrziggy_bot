package me.jonua.herrziggy_bot.command.handlers;

import me.jonua.herrziggy_bot.command.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

public abstract class BaseCommandHandler implements CommandHandler {
    public void handleCommand(BotCommand command, User from, Update update) {
        handleCommand(command, from, update, Map.of());
    }
}
