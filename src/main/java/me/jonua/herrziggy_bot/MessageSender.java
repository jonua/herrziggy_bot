package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSender {
    @Lazy
    @Autowired
    private HerrZiggyBot bot;

    public void send(String text, String sendTo, String parseMode) {
        try {
            String preparedText = text;
            if (ParseMode.MARKDOWNV2.equalsIgnoreCase(parseMode)) {
                preparedText = TelegramMessageUtils.tgEscape(parseMode, text);
            }

            SendMessage message = new SendMessage(
                    String.valueOf(sendTo),
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
            log.error("Unable to send message '{}' to {}: {}", text, sendTo, e.getMessage(), e);
        }
    }

    public void send(SendMessage message) throws TelegramApiException {
        bot.execute(message);
    }
}
