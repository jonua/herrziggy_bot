package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.service.StorageService;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramGroupNotifier {
    private final TelegramMessageBuilderService telegramMessageBuilder;
    private final MessageSender messageSender;
    private final StorageService storageService;

    public void notifySubscribers(String chatId, ZoneId zoneId, Message mailMessage) throws MessagingException {
        log.info("New mail for group receiver {}", chatId);

        MailNotificationContext ctx = MailNotificationContext.fromMessage(mailMessage, zoneId);
        ctx.setTelegramChatId(chatId);
        ctx.setZoneId(zoneId);
        ctx.setTelegramMessageParseMode(ParseMode.MARKDOWNV2);

        List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> tgMessages = telegramMessageBuilder.buildFromMail(mailMessage, ctx);
        log.info("Mail for group {} parsed to {} tg messages", chatId, tgMessages.size());

        for (PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message> tgMessage : tgMessages.reversed()) {
            sendMessage(chatId, tgMessage, false);
        }
    }

    private void sendMessage(String chatId, PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message> tgMessage, boolean errorHandled) {
        try {
            switch (tgMessage) {
                case SendMessage message -> send(chatId, message);
                case SendPhoto message -> send(chatId, message);
                case SendDocument message -> send(chatId, message);
                default -> log.error("Can't send message: unsupported message type: {}", tgMessage.getClass());
            }
        } catch (Exception e) {
            if (tryHandle(e, chatId, tgMessage, errorHandled)) {
                log.warn("Exception handled: {}", e.getMessage());
                return;
            }

            log.error("Not handled exception: unable to send message {}: {}", tgMessage.getClass(), e.getMessage(), e);
        }
    }

    private boolean tryHandle(Exception e, String destinationChatId, PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message> tgMessage, boolean alreadyHandled) {
        if (alreadyHandled) {
            log.error("The error already handled: {}", e.getMessage());
            throw new RuntimeException("Already handled error", e);
        }

        if (e instanceof TelegramApiRequestException) {
            if (((TelegramApiRequestException) e).getParameters() != null) {
                Long migrateToChatId = ((TelegramApiRequestException) e).getParameters().getMigrateToChatId();
                if (migrateToChatId != null) {
                    String newSourceId = String.valueOf(migrateToChatId);
                    storageService.updateMigrateToChatId(destinationChatId, newSourceId);
                    sendMessage(newSourceId, tgMessage, true);
                    return true;
                }
            }
        }
        String message = String.format("Unable to sent message to group %s: %s", destinationChatId, e.getMessage());
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    private void send(String chatId, SendDocument message) throws TelegramApiException {
        log.info("The next message will be sent to:{} document {} ({}) with caption {}",
                chatId, message.getDocument().getAttachName(), message.getDocument().getMediaName(), message.getCaption());
        message.setChatId(chatId);
        messageSender.send(message);
    }

    private void send(String chatId, SendPhoto message) throws TelegramApiException {
        log.info("The next message will be sent to:{} photo {} ({}) with caption {}",
                chatId, message.getPhoto().getAttachName(), message.getPhoto().getMediaName(), message.getCaption());
        message.setChatId(chatId);
        messageSender.send(message);
    }

    private void send(String chatId, SendMessage message) throws TelegramApiException {
        log.info("The next message will be sent to:{} message {}: {}",
                chatId, message.getReplyMarkup(), message.getText());
        message.setChatId(chatId);
        messageSender.send(message);
    }
}
