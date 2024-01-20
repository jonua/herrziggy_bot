package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

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
                "#user_direct_message\nNew message from " + TelegramMessageUtils.extractUserInfo(update.getMessage().getFrom()) + ": " + update.getMessage().getText()
        );
        messageSender.sendSilently(message);
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
