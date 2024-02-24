package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectMessageFromAdminToUserUserFlow implements UserFlow {
    private final static String PARAM_SEND_TO = "sendTo";

    private final MessageSender messageSender;
    private final MessageHandlerService messageHandlerService;

    @Value("${bot.admin-telegram-id}")
    private Long adminId;

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return UserFlowType.DIRECT_MESSAGE_FROM_ADMIN_TO_USER_FLOW.equals(userFlowType);
    }

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        if (params.isEmpty()) {
            throw new UnsupportedOperationException("Source ID required for the user flow");
        }

        if (!params.containsKey(PARAM_SEND_TO)) {
            List<String> callbackData = (List<String>) params.get(UserFlow.PARAM_CALLBACK_DATA);
            Long sendToId = Long.parseLong(callbackData.get(1));
            log.debug("Admin requests to send a direct message to a user with id {}", sendToId);
            messageSender.sendSilently(adminId, String.format("Write text to send to user %d or /cancel", sendToId));

            Map<String, Object> newParams = Map.of(PARAM_SEND_TO, sendToId);
            messageHandlerService.waitUserFlow(adminId, UserFlowType.DIRECT_MESSAGE_FROM_ADMIN_TO_USER_FLOW, newParams);
        } else {
            Long sendTo = (Long) params.get(PARAM_SEND_TO);
            String text = update.getMessage().getText();

            log.debug("Admin send text {} to user {} as response to message", text, sendTo);
            messageSender.sendSilently(sendTo, text);
        }
    }
}
