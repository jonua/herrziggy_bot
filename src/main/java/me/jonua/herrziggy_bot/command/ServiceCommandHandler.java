package me.jonua.herrziggy_bot.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.feedback.FeedbackHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCommandHandler implements CommandHandler {
    private final FeedbackHandler feedbackHandler;

    public void handleCommand(Message message, BotCommand command) {
        log.trace("User {} cancelled feedback", message.getFrom());
        feedbackHandler.calcelFeedback(message.getFrom());
    }
}
