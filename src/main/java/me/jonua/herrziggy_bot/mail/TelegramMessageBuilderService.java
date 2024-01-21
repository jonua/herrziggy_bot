package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import javax.mail.Message;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageBuilderService {
    @Value("${bot.locale}")
    private Locale locale;

    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildFromMail(Message message, MailNotificationContext ctx) {
        return new TelegramMessageFromMailBuilder(ctx, locale)
                .buildTelegramMessages(message);
    }
}
