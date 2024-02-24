package me.jonua.herrziggy_bot.flow;

import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Map;

public interface UserFlow {
    String PARAM_CALLBACK_DATA = "callbackData";

    boolean isSupport(UserFlowType userFlowType);

    void perform(Update update);

    void perform(Update update, Map<String, Object> params);
}
