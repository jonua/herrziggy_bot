package me.jonua.herrziggy_bot.flow;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {
    private final static Cache<Long, UserFlowType> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .build();

    private final UserFlowService userFlowService;
    private final DefaultResponseUserFlow defaultUserFlow;

    public void waitUserFlow(User user, UserFlowType userFlowType) {
        log.trace("Wait feedback from: {}", user);
        CACHE.put(user.getId(), userFlowType);
    }

    public void handleMessage(User from, Update update) {
        Long fromId = from.getId();

        try {
            UserFlowType flowType = CACHE.getIfPresent(fromId);
            if (flowType != null) {
                log.debug("Found waiting flow handler for sender:{}: {}", fromId, flowType);
                userFlowService.perform(flowType, from, update);
            } else {
                log.debug("No waiting flow handlers found for sender:{}", fromId);
                defaultUserFlow.perform(update);
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
