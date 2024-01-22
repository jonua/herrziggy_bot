package me.jonua.herrziggy_bot.command.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.flow.UserFlowService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackCommandHandler extends BaseCommandHandler {
    private final UserFlowService userFlow;

    @Override
    public void handleCommand(BotCommand command, User from, Update update, Map<String, Object> payload) {
        userFlow.perform(UserFlowType.SEND_FEEDBACK_PROMPT_MESSAGE, from, update);
    }

    @Override
    public boolean isSupport(BotCommand command) {
        return BotCommandType.FEEDBACK.equals(command.getCommandType());
    }
}
