package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveFeedbackFromUserFlow implements UserFlow {
    @Value("${bot.feedback.thanks-for-feedback-message}")
    private String thanksForFeedbackMessage;

    @Value("${bot.feedback.sent-to-user-id}")
    private String sendFeedbackTo;

    private final MessageSender messageSender;
    private final MessageHandlerService messageHandler;

    @Override
    public void perform(Update update) {
        Long fromId = update.getMessage().getFrom().getId();
        try {
            log.info("Feedback received from {}: {}", fromId, update.getMessage().getText());
            messageSender.send(thanksForFeedbackMessage, String.valueOf(fromId), ParseMode.MARKDOWNV2);

            String newFeedbackMessage = String.format("#feedback\nNew feedback received from %s: %s",
                    TelegramMessageUtils.extractUserInfo(update.getMessage().getFrom()), update.getMessage().getText());
            messageSender.send(newFeedbackMessage, sendFeedbackTo, null);
        } finally {
            messageHandler.stopWaiting(fromId);
        }
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.RECEIVE_FEEDBACK.equals(userFlowType);
    }
}
