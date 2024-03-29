package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class CommandHandlerService {
    private final List<BaseCommandHandler> commandHandlers;

    private final EmptyCommandHandler emptyCommandHandler;

    public void handleCommand(BotCommand command, User from, Update update) {
        BaseCommandHandler handler = findHandler(command);
        if (log.isTraceEnabled()) {
            log.trace("Command:{} will be handled by:{} for sender:{}. Message {}",
                    command, handler.getClass(), update.getMessage().getFrom().getId(), update);
        } else {
            log.debug("Command:{} will be handled by:{} for sender {}",
                    command, handler.getClass(), update.getMessage().getFrom().getId());
        }

        handler.handleCommand(command, from, update, Map.of("respondWithNoDataMessage", true));
    }

    private BaseCommandHandler findHandler(BotCommand command) {
        return commandHandlers.stream()
                .filter(handler -> !handler.getClass().isAssignableFrom(emptyCommandHandler.getClass()))
                .filter(handler -> handler.isSupport(command))
                .findAny()
                .orElseGet(() -> emptyCommandHandler);
    }
}
