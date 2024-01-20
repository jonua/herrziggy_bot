package me.jonua.herrziggy_bot.flow;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandler {
    private final static Cache<Long, UserFlowType> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .build();

    private final UserFlowService userFlowService;

    public void waitUserFlow(User user, UserFlowType userFlowType) {
        log.trace("Wait feedback from: {}", user);
        CACHE.put(user.getId(), userFlowType);
    }

    public void handleMessage(Message message) {
        Optional<Long> fromIdOpt = Optional.ofNullable(message)
                .map(Message::getFrom)
                .map(User::getId);

        if (fromIdOpt.isEmpty()) {
            log.warn("Empty sender: {}", message);
            return;
        }

        Long fromId = fromIdOpt.get();

        try {
            UserFlowType flowType = CACHE.getIfPresent(fromId);
            if (flowType != null) {
                log.debug("Found waiting flow handler for sender:{}: {}", fromId, flowType);
                userFlowService.perform(flowType, message);
            } else {
                log.debug("No waiting flow handlers found for sender:{}", fromId);
            }
        } finally {
            stopWaiting(fromId);
        }
    }

    public void stopWaiting(Long waiterId) {
        log.debug("Invalidation a handler if it is exist for waiter:{}", waiterId);
        CACHE.invalidate(waiterId);
    }
}
