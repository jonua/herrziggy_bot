package me.jonua.herrziggy_bot;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.bot.HerrZiggyBot;
import me.jonua.herrziggy_bot.mail.GmailIncomingMailReader;
import me.jonua.herrziggy_bot.mail.MessageNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
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
