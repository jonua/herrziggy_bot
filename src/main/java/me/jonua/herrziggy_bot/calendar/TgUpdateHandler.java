package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.CommandHandlerService;
import me.jonua.herrziggy_bot.flow.MessageHandler;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgUpdateHandler {
    private final MessageHandler messageHandler;
    private final StorageService storage;
    private final CommandHandlerService commandHandlerServoce;

    public void handleUpdate(Update update) {
        Message message = update.getMessage();

        storage.upsertSource(update);

        if (message.isCommand()) {
            for (MessageEntity entity : message.getEntities()) {
                if (entity.getType().equalsIgnoreCase("bot_command")) {
                    BotCommand command = BotCommand.fromString(entity.getText());
                    commandHandlerServoce.handleCommand(command, message);
                }
            }
        } else if (message.isUserMessage()) {
            messageHandler.handleMessage(message);
        }
    }
}
