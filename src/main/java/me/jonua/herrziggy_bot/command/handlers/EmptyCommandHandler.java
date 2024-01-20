package me.jonua.herrziggy_bot.command.handlers;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
public class EmptyCommandHandler implements CommandHandler {
    @Override
    public void handleCommand(BotCommand command, User from, Update update) {
        log.warn("Unhandled command:{} for message {}", command, update);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return true;
    }
}
