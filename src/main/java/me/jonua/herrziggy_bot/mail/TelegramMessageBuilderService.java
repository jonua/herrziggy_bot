package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.mail.parser.Mime2TelegramInputMediaBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;

import javax.mail.Message;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageBuilderService {
    @Value("${bot.locale}")
    private Locale locale;

    public Pair<SendMessage, Map<String, List<InputMedia>>> buildFromMail(Message message, MailNotificationContext ctx) {
        return new Mime2TelegramInputMediaBuilder(ctx, locale)
                .parseAndBuild(message);
    }
}
