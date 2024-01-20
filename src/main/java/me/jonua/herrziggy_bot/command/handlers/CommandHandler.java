package me.jonua.herrziggy_bot.command.handlers;

import me.jonua.herrziggy_bot.command.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {
    void handleCommand(BotCommand command, Message message);

    boolean isSupport(BotCommand command);
}
