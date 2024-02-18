package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.handlers.CommandHandlerService;
import me.jonua.herrziggy_bot.flow.MessageHandlerService;
import me.jonua.herrziggy_bot.flow.UserFlowService;
import me.jonua.herrziggy_bot.service.StorageService;
import me.jonua.herrziggy_bot.service.admin.AdminCommandService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class TgUpdateHandler {
    private final MessageHandlerService messageHandler;
    private final StorageService storage;
    private final CommandHandlerService commandHandlerService;
    private final UserFlowService userFlowService;
    private final AdminCommandService adminCommandService;

    List<Function<Update, Optional<User>>> userExtractor = List.of(
            update -> Optional.of(update).map(Update::getMessage).map(Message::getFrom),
            update -> Optional.of(update).map(Update::getCallbackQuery).map(CallbackQuery::getFrom),
            update -> Optional.of(update).map(Update::getMyChatMember).map(ChatMemberUpdated::getFrom),
            update -> Optional.of(update).map(Update::getChannelPost).map(Message::getFrom)
    );

    List<Function<Update, Optional<Chat>>> chatExtractor = List.of(
            update -> Optional.of(update).map(Update::getMessage).map(Message::getChat),
            update -> Optional.of(update).map(Update::getCallbackQuery).map(CallbackQuery::getMessage).map(Message::getChat),
            update -> Optional.of(update).map(Update::getMyChatMember).map(ChatMemberUpdated::getChat),
            update -> Optional.of(update).map(Update::getChannelPost).map(Message::getChat)
    );

    public void handleUpdate(Update update) {
        Message message = update.getMessage();

        userExtractor.stream().map(f -> f.apply(update)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(storage::upsertSource);
        chatExtractor.stream().map(f -> f.apply(update)).filter(Optional::isPresent).map(Optional::get).findFirst().ifPresent(storage::upsertSource);

        if (message != null) {
            if (message.isCommand()) {
                for (MessageEntity entity : message.getEntities()) {
                    if (entity.getType().equalsIgnoreCase("bot_command")) {
                        BotCommand command = BotCommand.fromString(entity.getText());
                        commandHandlerService.handleCommand(command, update.getMessage().getFrom(), update);
                    }
                }
            } else if (adminCommandService.isAdminCommand(message)) {
                adminCommandService.handle(message, message.getFrom().getId());
            } else if (message.isUserMessage()) {
                messageHandler.handleMessage(update.getMessage().getFrom(), update);
            }
        } else if (update.hasCallbackQuery()) {
            if (!userFlowService.performIsFlow(update)) {
                messageHandler.handleMessage(update.getCallbackQuery().getFrom(), update);
            }
        } else {
            log.trace("Unhandled update: {}", update);
        }
    }
}
