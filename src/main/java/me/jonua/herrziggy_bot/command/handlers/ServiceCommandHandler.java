package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.CommandType;
import me.jonua.herrziggy_bot.flow.MessageHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCommandHandler implements CommandHandler {
    private final MessageHandler messageHandler;

    public void handleCommand(BotCommand command, Message message) {
        log.trace("User {} cancelled feedback", message.getFrom());
        messageHandler.stopWaiting(message.getFrom().getId());
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return CommandType.SERVICE.equals(command.getCommandType());
    }
}
