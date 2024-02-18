package me.jonua.herrziggy_bot.enums;

import lombok.Getter;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;

@Getter
public enum AdminCommandOptions {
    CONGRATULATION_ON_8_MARCH("Отправить поздравление с 8 марта", UserFlowType.CONGRATULATION_ON_8_MARCH),
    STAT_NEW_USERS("Статистика новых пользователей", UserFlowType.SHOW_STAT_NEW_USERS),
    STAT_ACTIVE_USERS("Статистика активных пользователей", UserFlowType.SHOW_STAT_ACTIVE_USERS),
    ;

    private final String commandName;
    private final UserFlowType userFlowType;

    AdminCommandOptions(String commandName, UserFlowType userFlowType) {
        this.commandName = commandName;
        this.userFlowType = userFlowType;
    }
}
