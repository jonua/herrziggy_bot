package me.jonua.herrziggy_bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramGroupMessageSender {
    private final AbsSender sender;
    @Value("${bot.chat-id}")
    private String chatId;

    public void sendMessage(String message) {
        try {
            log.info("Sending message to {}", chatId);
            SendMessage sendMessage = new SendMessage(
                    chatId,
                    null,
                    message,
                    null,
                    true,
                    false,
                    null,
                    null,
                    null,
                    null,
                    false
            );
            sender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Unable to send message {}: {}", message, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
