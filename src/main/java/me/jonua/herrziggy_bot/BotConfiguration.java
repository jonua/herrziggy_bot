package me.jonua.herrziggy_bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class BotConfiguration {
    @Value("${bot.token}")
    private String botToken;



    @Bean
    public HerrZiggyBot herrZiggyBot() {
        try {
            HerrZiggyBot bot = new HerrZiggyBot(new DefaultBotOptions(), botToken);

            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
            return bot;
        } catch (TelegramApiException e) {
            log.error("Unable to register bot: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
