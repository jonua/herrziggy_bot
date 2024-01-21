package me.jonua.herrziggy_bot.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.CommandHandlerService;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import me.jonua.herrziggy_bot.flow.UserFlowService;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class TgUpdateHandler {
    private final MessageHandlerService messageHandler;
    private final StorageService storage;
    private final CommandHandlerService commandHandlerService;
    private final UserFlowService userFlowService;

    public void handleUpdate(Update update) {
        Message message = update.getMessage();


        if (message != null) {
            storage.upsertSourceUser(update.getMessage().getFrom());
            storage.upsertSourceChat(update.getMessage().getChat());

            if (message.isCommand()) {
                for (MessageEntity entity : message.getEntities()) {
                    if (entity.getType().equalsIgnoreCase("bot_command")) {
                        BotCommand command = BotCommand.fromString(entity.getText());
                        commandHandlerService.handleCommand(command, update.getMessage().getFrom(), update);
                    }
                }
            } else if (message.isUserMessage()) {
                messageHandler.handleMessage(update.getMessage().getFrom(), update);
            }
        } else if (update.hasCallbackQuery()) {
            storage.upsertSourceUser(update.getCallbackQuery().getFrom());
            storage.upsertSourceChat(update.getCallbackQuery().getMessage().getChat());
            if (!userFlowService.callFlow(update)) {
                messageHandler.handleMessage(update.getCallbackQuery().getFrom(), update);
            }
        } else {
            log.error("Unhandled update: {}", update);
        }
    }
}
