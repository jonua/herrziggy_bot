package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import javax.mail.Message;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageBuilderService {
    @Value("${default-zone-id}")
    private ZoneId zoneId;
    @Value("${bot.max-allowed-entity-size-bytes}")
    private Integer attachmentSizeThresholdBytes;

    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildFromMail(Message message, String chatId) {
        return new TelegramMessageFromMailBuilder(chatId, zoneId, attachmentSizeThresholdBytes)
                .buildTelegramMessages(message);
    }
}
