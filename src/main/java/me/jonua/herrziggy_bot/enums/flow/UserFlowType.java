package me.jonua.herrziggy_bot.enums.flow;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum UserFlowType {
    SEND_FEEDBACK_PROMPT_MESSAGE(UserFlowType.NOT_A_COMMAND),
    RECEIVE_FEEDBACK(UserFlowType.NOT_A_COMMAND),
    RECEIVE_NEW_CALENDAR_CONFIG("cf"),
    CONGRATULATION_ON_8_MARCH("8march"),
    ;

    private final static String NOT_A_COMMAND = "-not a command-";

    private final String commandPrefix;

    UserFlowType(String commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    public static Optional<Pair<UserFlowType, List<String>>> parseCommandAndData(String rawData) {
        if (rawData == null) {
            return Optional.empty();
        }

        String[] parts = rawData.split(":");
        for (UserFlowType type : UserFlowType.values()) {
            if (type.getCommandPrefix().equalsIgnoreCase(parts[0])) {
                return Optional.of(Pair.of(type, Arrays.stream(Arrays.copyOfRange(parts, 1, parts.length)).toList()));
            }
        }

        return Optional.empty();
    }

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
