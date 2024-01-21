package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class UserFlowService {
    private final List<UserFlow> userFlows;
    private final EmptyFlow emptyFlow;

    public void perform(UserFlowType userFlowType, User from, Update update) {
        UserFlow flow = findFlow(userFlowType);
        if (log.isTraceEnabled()) {
            log.trace("FLow:{} will be performed by:{} for sender:{}. Message {}",
                    userFlowType, flow.getClass(), from.getId(), update);
        } else {
            log.debug("FLow:{} will be performed by:{} for sender {}",
                    userFlowType, flow.getClass(), from.getId());
        }

        flow.perform(update);
    }

    private UserFlow findFlow(UserFlowType userFlowType) {
        return userFlows.stream()
                .filter(flow -> !flow.getClass().isAssignableFrom(EmptyFlow.class))
                .filter(flow -> flow.isSupport(userFlowType))
                .findAny()
                .orElseGet(() -> emptyFlow);
    }

    public boolean callFlow(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String[] parts = callbackData.split(":");
            if (parts[0].equalsIgnoreCase("cf") && parts[1].equalsIgnoreCase("calendar")) {
                perform(UserFlowType.RECEIVE_NEW_CALENDAR_CONFIG, update.getCallbackQuery().getFrom(), update);
                return true;
            }
        }

        if (log.isTraceEnabled()) {
            log.debug("Not a callflow update:{}", update);
        } else {
            log.debug("Not a callflow update");
        }

        return false;
    }
}
