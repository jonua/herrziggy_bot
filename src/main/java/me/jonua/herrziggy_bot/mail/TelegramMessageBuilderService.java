package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import javax.mail.Message;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageBuilderService {
    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildFromMail(Message message, MailNotificationContext ctx) {
        return new TelegramMessageFromMailBuilder(ctx)
                .buildTelegramMessages(message);
    }
}
