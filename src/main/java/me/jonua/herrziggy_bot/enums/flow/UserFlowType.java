package me.jonua.herrziggy_bot.enums.flow;

import org.apache.commons.lang3.StringUtils;

public enum UserFlowType {
    SEND_FEEDBACK_PROMPT_MESSAGE,
    RECEIVE_FEEDBACK,
    RECEIVE_NEW_CALENDAR_CONFIG,
    ;

    public static UserFlowType fromString(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return null;
        }

        for (UserFlowType flow : UserFlowType.values()) {
            if (flow.name().equalsIgnoreCase(value)) {
                return flow;
            }
        }

        return null;
    }
}
