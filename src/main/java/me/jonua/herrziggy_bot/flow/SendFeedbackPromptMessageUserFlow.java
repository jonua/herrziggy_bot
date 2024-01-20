package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class SendFeedbackPromptMessageUserFlow implements UserFlow {
    @Value("${bot.feedback.give-feedback-message}")
    private String giveFeedbackMessage;
    private final MessageHandlerService feedbackHandler;
    private final MessageSender messageSender;

    @Override
    public void perform(Update message) {
        feedbackHandler.waitUserFlow(message.getMessage().getFrom(), UserFlowType.RECEIVE_FEEDBACK);
        messageSender.send(giveFeedbackMessage, String.valueOf(message.getMessage().getFrom().getId()), null);
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.SEND_FEEDBACK_PROMPT_MESSAGE.equals(userFlowType);
    }
}