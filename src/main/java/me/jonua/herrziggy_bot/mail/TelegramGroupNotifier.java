package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.HerrZiggyBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramGroupNotifier {
    @Value("${bot.chat-id}")
    private String chatId;
    @Value("${default-zone-id}")
    private ZoneId zoneId;
    @Value("${bot.max-allowed-entity-size-bytes}")
    private Integer attachmentSizeThresholdBytes;

    private final HerrZiggyBot bot;
    private final TelegramMessageBuilderService telegramMessageBuilder;

    public void notifySubscribers(Message mailMessage) throws MessagingException {
        MailNotificationContext ctx = MailNotificationContext.fromMessage(mailMessage, zoneId);
        ctx.setTelegramChatId(chatId);
        ctx.setZoneId(zoneId);
        ctx.setAttachmentSizeThresholdBytes(attachmentSizeThresholdBytes);
        ctx.setTelegramMessageParseMode(ParseMode.MARKDOWNV2);

        List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> tgMessages = telegramMessageBuilder.buildFromMail(mailMessage, ctx);
        for (PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message> tgMessage : tgMessages.reversed()) {
            try {
                switch (tgMessage) {
                    case SendMessage message -> bot.execute(message);
                    case SendPhoto message -> bot.execute(message);
                    case SendDocument message -> bot.execute(message);
                    default -> log.error("Can't send message: unsupported message type: {}", tgMessage.getClass());
                }
            } catch (Exception e) {
                log.error("Unable to send message {}: {}", tgMessage.getClass(), e.getMessage(), e);
            }
        }
    }
}
