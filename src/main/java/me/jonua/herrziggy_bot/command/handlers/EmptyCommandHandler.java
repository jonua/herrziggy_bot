package me.jonua.herrziggy_bot.command.handlers;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.CommandHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
public class EmptyCommandHandler implements CommandHandler {
    @Override
    public void handleCommand(BotCommand command, Message message) {
        log.warn("Unhandled command:{} for message {}", command, message);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return true;
    }
}
