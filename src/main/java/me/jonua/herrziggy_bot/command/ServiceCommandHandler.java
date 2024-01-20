package me.jonua.herrziggy_bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.flow.MessageHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCommandHandler implements CommandHandler {
    private final MessageHandler messageHandler;

    public void handleCommand(Message message, BotCommand command) {
        log.trace("User {} cancelled feedback", message.getFrom());
        messageHandler.stopWaiting(message.getFrom().getId());
    }
}
