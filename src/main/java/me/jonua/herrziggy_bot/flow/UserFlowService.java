package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Lazy))
public class UserFlowService {
    private final List<UserFlow> userFlows;
    private final EmptyFlow emptyFlow;

    public void perform(UserFlowType userFlowType, User from, Update update) {
        perform(userFlowType, from, update, List.of());
    }

    public void perform(UserFlowType userFlowType, User from, Update update, List<String> commandCallbackData) {
        UserFlow flow = findFlow(userFlowType);
        if (log.isTraceEnabled()) {
            log.trace("Flow:{} will be performed by:{} for sender:{}. Message {}",
                    userFlowType, flow.getClass(), from.getId(), update);
        } else {
            log.debug("Flow:{} will be performed by:{} for sender {}",
                    userFlowType, flow.getClass(), from.getId());
        }

        flow.perform(update, commandCallbackData);
    }

    public boolean performIsFlow(Update update) {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            Optional<Pair<UserFlowType, List<String>>> flowCommand = UserFlowType.parseCommandAndData(callbackData);
            if (flowCommand.isPresent()) {
                perform(
                        flowCommand.map(Pair::getFirst).get(),
                        update.getCallbackQuery().getFrom(),
                        update,
                        flowCommand.map(Pair::getSecond).get()
                );
                return true;
            }
        }

        log.warn("The update does not contain any user flow command");
        return false;
    }

    private UserFlow findFlow(UserFlowType userFlowType) {
        return userFlows.stream()
                .filter(flow -> !flow.getClass().isAssignableFrom(EmptyFlow.class))
                .filter(flow -> flow.isSupport(userFlowType))
                .findAny()
                .orElseGet(() -> emptyFlow);
    }
}
