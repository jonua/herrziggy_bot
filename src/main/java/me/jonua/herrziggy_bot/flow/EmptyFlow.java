package me.jonua.herrziggy_bot.flow;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class EmptyFlow implements UserFlow {
    @Override
    public void perform(Update update) {
        log.warn("Empty flow triggered for message {}", update);
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
