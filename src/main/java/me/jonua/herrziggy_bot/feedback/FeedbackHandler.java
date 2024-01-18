package me.jonua.herrziggy_bot.feedback;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackHandler {
    private final static Cache<Long, Object> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    @Value("${bot.feedback.thanks-for-feedback-message}")
    private String thanksForFeedbackMessage;

    @Value("${bot.feedback.sent-to-user-id}")
    private String sendFeedbackTo;

    private final MessageSender messageSender;

    public void waitForFeedbackFrom(User user) {
        log.trace("Wait feedback from: {}", user);
        CACHE.put(user.getId(), 1L);
    }

    public void handleFeedback(Message message) {
        Optional<Long> fromIdOpt = Optional.ofNullable(message)
                .map(Message::getFrom)
                .map(User::getId);

        if (fromIdOpt.isEmpty()) {
            log.warn("Empty sender: {}", message);
            return;
        }

        Long fromId = fromIdOpt.get();

        try {
            if (CACHE.getIfPresent(fromId) != null) {
                log.info("Feedback received from {}: {}", fromId, message.getText());
                messageSender.send(thanksForFeedbackMessage, String.valueOf(fromId), ParseMode.MARKDOWNV2);

                String newFeedbackMessage = String.format("#feedback\n\nNew feedback received from @%s: %s",
                        message.getFrom().getUserName(), message.getText());
                messageSender.send(newFeedbackMessage, sendFeedbackTo, null);
            }
        } finally {
            CACHE.invalidate(fromId);
        }
    }

    public void calcelFeedback(User from) {
        log.trace("Feedback cancelled by {}", from);
        CACHE.invalidate(from.getId());
    }
}
