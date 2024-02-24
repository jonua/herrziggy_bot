package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveFeedbackFromUserFlow implements UserFlow {
    @Value("${messages.feedback-thanks-message}")
    private String thanksForFeedbackMessage;

    @Value("${bot.admin-telegram-id}")
    private Long sendFeedbackTo;

    private final MessageSender messageSender;
    private final MessageHandlerService messageHandler;

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        Message message = update.getMessage();
        Long fromId = message.getFrom().getId();
        try {
            log.info("Feedback received from {}: {}", fromId, message.getText());
            sendThanksMessage(fromId);
            sendFeedbackToAdmin(fromId, message);
        } finally {
            messageHandler.stopWaiting(fromId);
        }
    }

    private void sendFeedbackToAdmin(Long fromId, Message message) {
        String userInfo = TelegramMessageUtils.extractUserInfo(message.getFrom());
        String newFeedbackMessage = String.format("#feedback\nNew feedback received from %s: %s",
                userInfo, message.getText());

        InlineKeyboardMarkup keyboardMarkup = TelegramMessageUtils.buildInlineKeyboardMarkup(
                List.of(new TelegramMessageUtils.KeyboardButton(
                        "Respond to " + userInfo,
                        UserFlowType.DIRECT_MESSAGE_FROM_ADMIN_TO_USER_FLOW.getCommandPrefix() + ":" + fromId)
                ),
                1);

        try {
            messageSender.send(newFeedbackMessage, keyboardMarkup, sendFeedbackTo);
        } catch (TelegramApiException e) {
            log.error("Unable send message with keyboard markup to {}: {}", sendFeedbackTo, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void sendThanksMessage(Long fromId) {
        messageSender.sendSilently(thanksForFeedbackMessage, String.valueOf(fromId), ParseMode.MARKDOWNV2);
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.RECEIVE_FEEDBACK.equals(userFlowType);
    }
}
