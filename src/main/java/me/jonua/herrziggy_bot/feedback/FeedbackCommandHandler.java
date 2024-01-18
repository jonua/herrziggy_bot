package me.jonua.herrziggy_bot.feedback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.CommandHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackCommandHandler implements CommandHandler {
    @Value("${bot.feedback.give-feedback-message}")
    private String giveFeedbackMessage;
    private final FeedbackHandler feedbackHandler;
    private final MessageSender messageSender;

    @Override
    public void handleCommand(Message message, BotCommand command) {
        feedbackHandler.waitForFeedbackFrom(message.getFrom());
        messageSender.send(giveFeedbackMessage, String.valueOf(message.getFrom().getId()), null);
    }
}
