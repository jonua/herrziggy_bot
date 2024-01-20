package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveFeedbackFromUserFlow implements UserFlow {
    @Value("${bot.feedback.thanks-for-feedback-message}")
    private String thanksForFeedbackMessage;

    @Value("${bot.feedback.sent-to-user-id}")
    private String sendFeedbackTo;

    private final MessageSender messageSender;
    private final MessageHandler messageHandler;

    @Override
    public void perform(Message message) {
        Long fromId = message.getFrom().getId();
        try {
            log.info("Feedback received from {}: {}", fromId, message.getText());
            messageSender.send(thanksForFeedbackMessage, String.valueOf(fromId), ParseMode.MARKDOWNV2);

            String newFeedbackMessage = String.format("#feedback\n\nNew feedback received from @%s: %s",
                    message.getFrom().getUserName(), message.getText());
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
