package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSender {
    @Lazy
    @Autowired
    private HerrZiggyBot bot;

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
}
