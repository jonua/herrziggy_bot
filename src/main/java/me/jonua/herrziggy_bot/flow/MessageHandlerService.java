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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageHandlerService {
    private final static Cache<Long, Map<String, Object>> CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .build();
    public static final String FLOW_TYPE = "flowType";

    private final UserFlowService userFlowService;
    private final DefaultResponseUserFlow defaultUserFlow;

    public void waitUserFlow(Long userId, UserFlowType userFlowType) {
        waitUserFlow(userId, userFlowType, Map.of());
    }

    public void waitUserFlow(Long userId, UserFlowType userFlowType, Map<String, Object> params) {
        log.trace("Wait feedback from: {}", userId);
        Map<String, Object> localParams = new HashMap<>(params);
        localParams.put(FLOW_TYPE, userFlowType);

        CACHE.put(userId, localParams);
    }

    public void handleMessage(User from, Update update) {
        Long fromId = from.getId();

        try {
            Map<String, Object> params = CACHE.getIfPresent(fromId);
            if (params != null) {
                UserFlowType flowType = (UserFlowType) params.get(FLOW_TYPE);
                log.debug("Found waiting flow handler for sender:{}: {}", fromId, flowType);
                userFlowService.perform(flowType, from, update, params);
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
