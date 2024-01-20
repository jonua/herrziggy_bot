package me.jonua.herrziggy_bot.flow;

import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserFlow {
    void perform(Update update);

    boolean isSupport(UserFlowType userFlowType);
}
