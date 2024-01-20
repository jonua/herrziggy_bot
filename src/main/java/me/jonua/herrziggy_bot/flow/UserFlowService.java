package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class UserFlowService {
    private final List<UserFlow> userFlows;
    private final EmptyFlow emptyFlow;

    public void perform(UserFlowType userFlowType, Message message) {
        findFlow(userFlowType).perform(message);
    }

    private UserFlow findFlow(UserFlowType userFlowType) {
        return userFlows.stream()
                .filter(flow -> !flow.getClass().isAssignableFrom(EmptyFlow.class))
                .filter(flow -> flow.isSupport(userFlowType))
                .findAny()
                .orElseGet(() -> emptyFlow);
    }
}
