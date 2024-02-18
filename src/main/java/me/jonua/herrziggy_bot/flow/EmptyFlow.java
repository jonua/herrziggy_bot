package me.jonua.herrziggy_bot.flow;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Service
public class EmptyFlow implements UserFlow {
    @Override
    public void perform(Update update) {
        perform(update, List.of());
    }

    @Override
    public void perform(Update update, List<String> commandCallbackData) {
        log.warn("Empty flow triggered for message {}", update);
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
