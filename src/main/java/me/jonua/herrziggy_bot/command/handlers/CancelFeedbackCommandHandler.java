package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelFeedbackCommandHandler extends BaseCommandHandler {
    private final MessageHandlerService messageHandler;

    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        log.trace("User {} cancelled feedback", update.getMessage().getFrom());
        messageHandler.stopWaiting(update.getMessage().getFrom().getId());
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommandType.CANCEL_FEEDBACK.equals(command.getCommandType());
    }
}
