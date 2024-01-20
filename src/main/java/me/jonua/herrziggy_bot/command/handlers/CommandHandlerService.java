package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandHandlerService {
    private final List<CommandHandler> commandHandlers;

    private final EmptyCommandHandler emptyCommandHandler;

    public void handleCommand(BotCommand command, Message message) {
        CommandHandler handler = findHandler(command);
        if (log.isTraceEnabled()) {
            log.trace("Command:{} will be handled by:{} for sender:{}. Message {}",
                    command, handler.getClass(), message.getFrom().getId(), message);
        } else {
            log.debug("Command:{} will be handled by:{} for sender {}",
                    command, handler.getClass(), message.getFrom().getId());
        }

        handler.handleCommand(command, message);
    }

    private CommandHandler findHandler(BotCommand command) {
        return commandHandlers.stream()
                .filter(handler -> !handler.getClass().isAssignableFrom(emptyCommandHandler.getClass()))
                .filter(handler -> handler.isSupport(command))
                .findAny()
                .orElseGet(() -> emptyCommandHandler);
    }
}
