package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MessageSender {
    private final HerrZiggyBot bot;

    public void send(String text, InlineKeyboardMarkup keyboardMarkup, Long sendToId) throws TelegramApiException {
        send(text, keyboardMarkup, String.valueOf(sendToId));
    }

    public void send(String text, InlineKeyboardMarkup keyboardMarkup, String sendToId) throws TelegramApiException {
        log.info("Sending keyboard merkup with text {} to {}", text, sendToId);

        SendMessage tgMessage = new SendMessage(
                sendToId,
                null,
                text,
                null,
                false,
                false,
                null,
                keyboardMarkup,
                null,
                true,
                false
        );

        send(tgMessage);
    }

    public void send(String text, Long sendToId) {
        send(text, String.valueOf(sendToId), null);
    }

    public void send(String text, String sendToId, String parseMode) {
        if (StringUtils.isEmpty(text)) {
            log.warn("Text can't be empty");
            return;
        }

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

    public void sendSilently(SendMediaGroup group) {
        try {
            send(group);
        } catch (TelegramApiException e) {
            log.error("Unable to send media group: {}", e.getMessage(), e);
        }
    }


    public void sendSilently(SendSticker sticker) {
        try {
            send(sticker);
        } catch (TelegramApiException e) {
            log.error("Unable to send sticker: {}", e.getMessage(), e);
        }
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

    public <T extends Serializable> Optional<T> sendSilently(BotApiMethod<T> message) {
        try {
            return Optional.of(send(message));
        } catch (TelegramApiException e) {
            log.error("Unable to sent message: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    public <T extends Serializable> T send(BotApiMethod<T> message) throws TelegramApiException {
        log.info("Sending message {} ...", message.getClass());
        return bot.execute(message);
    }

    public Message send(SendDocument message) throws TelegramApiException {
        log.info("Sending document message to {} ...", message.getChatId());
        return bot.execute(message);
    }

    public Message send(SendPhoto message) throws TelegramApiException {
        log.info("Sending photo message to {} ...", message.getChatId());
        return bot.execute(message);
    }

    public Message send(SendAudio message) throws TelegramApiException {
        log.info("Sending audio message to {} ...", message.getChatId());
        return bot.execute(message);
    }

    public Message send(SendVideo message) throws TelegramApiException {
        log.info("Sending video message to {} ...", message.getChatId());
        return bot.execute(message);
    }

    public void send(SendSticker sticker) throws TelegramApiException {
        log.info("Sending sticker to {} ...", sticker.getChatId());
        bot.execute(sticker);
    }

    public void send(SendMediaGroup group) throws TelegramApiException {
        log.info("Sending media group to {} ...", group.getChatId());
        bot.execute(group);
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
        try {
            bot.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to delete message {} in chat {}: {}", messageId, chatId, e.getMessage(), e);
        }
    }
}
