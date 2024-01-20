package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class DefaultUserFlow implements UserFlow {
    @Value("${bot.feedback.sent-to-user-id}")
    private String botAdminUserId;

    private final MessageSender messageSender;

    @Override
    public void perform(Update update) {
        SendMessage message = new SendMessage(
                botAdminUserId,
                "#user_direct_message\nNew message from " + buildUserInfo(update) + ": " + update.getMessage().getText()
        );
        messageSender.sendSilently(message);
    }

    private String buildUserInfo(Update update) {
        String unknownUser = "-unknown user-";
        Message message = update.getMessage();
        if (message == null) {
            return unknownUser;
        }

        String result = "";
        User from = message.getFrom();
        if (StringUtils.isNotEmpty(from.getFirstName())) {
            if (StringUtils.isNotEmpty(from.getLastName())) {
                result = String.format("%s %s", from.getFirstName(), from.getLastName());
            } else {
                result = from.getFirstName();
            }
        }

        if (!StringUtils.isEmpty(from.getUserName())) {
            if (StringUtils.isNotEmpty(result)) {
                result = "@" + from.getUserName() + " (" + result + ")";
            } else {
                result = "@" + from.getUserName();
            }
        }

        result += " [" + from.getId() + "]";

        return result;
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
