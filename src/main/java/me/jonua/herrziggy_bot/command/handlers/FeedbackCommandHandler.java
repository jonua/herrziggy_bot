package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.CommandType;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.flow.UserFlowService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackCommandHandler implements CommandHandler {
    private final UserFlowService userFlow;

    @Override
    public void handleCommand(BotCommand command, Message message) {
        userFlow.perform(UserFlowType.SEND_FEEDBACK_PROMPT_MESSAGE, message);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return CommandType.FEEDBACK.equals(command.getCommandType());
    }
}
