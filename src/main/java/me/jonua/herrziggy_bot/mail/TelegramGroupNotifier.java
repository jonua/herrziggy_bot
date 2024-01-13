package me.jonua.herrziggy_bot.mail;

import jakarta.activation.MimeTypeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.TelegramGroupMessageSender;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class TelegramGroupNotifier {
    private final TelegramGroupMessageSender notifier;
    private final String zoneId;
    private final MailMessageParser mainMessageParser;

    public void notifySubscribers(Message message) {
        try {
            String textFromMessage = mainMessageParser.getTextFromMessage(message);

            ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of(zoneId));
            String date = DateTimeFormatter.ofPattern("yyyy_MM_dd").format(zdt);
            String telegramMessage = String.format("""
                    __от__: **%s**
                    __тема__: **%s**
                    ___

                    %s

                    ___
                    #mailmessage #date_%s
                    """, "mailFrom", "mailSubject", textFromMessage, date);

            notifier.sendMessage(telegramMessage);
        } catch (MessagingException | IOException e) {
            log.error("Unable notify subscribers: {}", e.getMessage(), e);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }
}
