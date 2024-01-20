package me.jonua.herrziggy_bot.flow;

import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserFlow {
    void perform(Message message);

    boolean isSupport(UserFlowType userFlowType);
}
