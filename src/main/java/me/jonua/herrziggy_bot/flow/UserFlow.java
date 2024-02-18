package me.jonua.herrziggy_bot.flow;

import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UserFlow {
    boolean isSupport(UserFlowType userFlowType);

    void perform(Update update);

    void perform(Update update, List<String> commandCallbackData);
}
