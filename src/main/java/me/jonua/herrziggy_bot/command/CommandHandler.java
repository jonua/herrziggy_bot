package me.jonua.herrziggy_bot.command;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface CommandHandler {
    void handleCommand(Message message, BotCommand command);
}
