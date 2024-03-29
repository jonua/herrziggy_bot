package me.jonua.herrziggy_bot.command.handlers;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

@Slf4j
@Service
public class EmptyCommandHandler extends BaseCommandHandler {
    @Override
    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        log.warn("Unhandled command:{} for message {}", command, update);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return true;
    }
}
