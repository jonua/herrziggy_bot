package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Slf4j
@RequiredArgsConstructor
public class MessageSender {
    private final HerrZiggyBot bot;

    public void send(String text, Long sendToId) {
        send(text, String.valueOf(sendToId), null);
    }

    public void send(String text, String sendToId, String parseMode) {
        try {
            String preparedText = text;
            if (ParseMode.MARKDOWNV2.equalsIgnoreCase(parseMode)) {
                preparedText = TelegramMessageUtils.tgEscape(parseMode, text);
            }

            SendMessage message = new SendMessage(
                    String.valueOf(sendToId),
                    null,
                    preparedText,
                    parseMode,
                    false,
                    false,
                    null,
                    null,
                    null,
                    true,
                    false
            );

            send(message);
        } catch (TelegramApiException e) {
            log.error("Unable to send message '{}' to {}: {}", text, sendToId, e.getMessage(), e);
        }
    }

    public <T extends Serializable> void send(BotApiMethod<T> message) throws TelegramApiException {
        bot.execute(message);
    }

    public void send(SendDocument message) throws TelegramApiException {
        bot.execute(message);
    }

    public void send(SendPhoto message) throws TelegramApiException {
        bot.execute(message);
    }

    public void sendSilently(SendSticker sticker) {
        try {
            send(sticker);
        } catch (TelegramApiException e) {
            log.error("Unable to send sticker: {}", e.getMessage(), e);
        }
    }

    public void send(SendSticker sticker) throws TelegramApiException {
        bot.execute(sticker);
    }

    public <T extends Serializable> void sendSilently(Long replyTo, String text) {
        sendSilently(replyTo, text, null, null);
    }

    public <T extends Serializable> void sendSilently(Long replyTo, String text, String parseMode) {
        sendSilently(replyTo, text, null, parseMode);
    }

    public <T extends Serializable> void sendSilently(Long replyTo, String text, Integer replyToMessageId) {
        sendSilently(replyTo, text, replyToMessageId, null);
    }

    public <T extends Serializable> void sendSilently(Long replyTo, String text, Integer replyToMessageId, String parseMode) {
        String textMessage = text;

        if (ParseMode.MARKDOWNV2.equalsIgnoreCase(parseMode)) {
            textMessage = TelegramMessageUtils.tgEscape(parseMode, text);
        }

        SendMessage sendMessage = new SendMessage(
                String.valueOf(replyTo),
                null,
                textMessage,
                parseMode,
                null,
                false,
                replyToMessageId,
                null,
                null,
                true,
                false
        );

        sendSilently(sendMessage);
    }

    public <T extends Serializable> void sendSilently(BotApiMethod<T> message) {
        try {
            send(message);
        } catch (TelegramApiException e) {
            log.error("Unable to sent message: {}", e.getMessage(), e);
        }
    }
}
