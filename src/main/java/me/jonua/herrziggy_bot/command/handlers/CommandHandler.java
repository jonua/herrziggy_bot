package me.jonua.herrziggy_bot.command.handlers;

import me.jonua.herrziggy_bot.command.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public interface CommandHandler {
    void handleCommand(BotCommand command, User from, Update update);

    boolean isSupport(BotCommand command);
}
