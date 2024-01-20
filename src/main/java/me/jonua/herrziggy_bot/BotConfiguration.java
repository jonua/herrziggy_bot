package me.jonua.herrziggy_bot;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.calendar.TgUpdateHandler;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.command.BotCommandType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.AllowedUpdates;
import org.telegram.telegrambots.meta.api.objects.WebhookInfo;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class BotConfiguration {
    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.feedback.enabled:false}")
    private Boolean feedbackEnabled;

    @Autowired
    private TgUpdateHandler calendarAdapter;

    @Bean
    public HerrZiggyBot herrZiggyBot() {
        try {
            HerrZiggyBot bot = new HerrZiggyBot(new DefaultBotOptions(), botToken, calendarAdapter);

            List<BotCommandType> commandTypes = new ArrayList<>();
            commandTypes.add(BotCommandType.CALENDAR);

            log.info("feedback feature enabled: " + feedbackEnabled);

            if (feedbackEnabled) {
                commandTypes.add(BotCommandType.FEEDBACK);

                WebhookInfo webhookInfo = bot.getWebhookInfo();
                webhookInfo.setAllowedUpdates(List.of(AllowedUpdates.MESSAGE));
            }

            initializeCommands(bot, commandTypes);

            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
            return bot;
        } catch (TelegramApiException e) {
            log.error("Unable to register bot: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void initializeCommands(HerrZiggyBot bot, List<BotCommandType> types) throws TelegramApiException {
        List<? extends org.telegram.telegrambots.meta.api.objects.commands.BotCommand> botCommands = Arrays.stream(BotCommand.values())
                .filter(command -> types.contains(command.getCommandType()))
                .map(botCommand -> org.telegram.telegrambots.meta.api.objects.commands.BotCommand.builder().command(botCommand.getCommand()).description(botCommand.getDescription()).build())
                .toList();

        bot.execute(SetMyCommands.builder()
                .commands(botCommands)
                .scope(BotCommandScopeAllPrivateChats.builder().build()).build());
    }
}
