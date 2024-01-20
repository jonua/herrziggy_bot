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
        Message message = update.getMessage();
        String result = "-unknown user-";
        if (message == null) {
            return result;
        }

        User from = message.getFrom();
        result = String.format("%s %s", from.getFirstName(), from.getLastName());
        if (!StringUtils.isEmpty(from.getUserName())) {
            result = "@" + from.getUserName() + " (" + result + ")";
        }

        return result;
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
