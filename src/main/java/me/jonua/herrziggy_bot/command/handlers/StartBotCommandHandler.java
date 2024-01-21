package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class StartBotCommandHandler implements CommandHandler {
    private final CommandHandlerService commandHandlerService;

    @Override
    public void handleCommand(BotCommand command, User from, Update update) {
        commandHandlerService.handleCommand(BotCommand.SETUP_USER_CALENDAR, from, update);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommand.START_BOT.equals(command);
    }
}
