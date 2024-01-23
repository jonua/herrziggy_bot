package me.jonua.herrziggy_bot.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${default-zone-id}")
    private ZoneId defaultZoneId;

    @Autowired
    private MailTgGroupNotifierConfiguration mailConfigurations;

    @Autowired
    private TelegramGroupNotifier notifier;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for (MailTgGroupNotifierConfiguration.Notifier notifier : mailConfigurations.getNotifiers()) {
            if (!notifier.getEnabled()) {
                log.warn("Mail notificator disabled for {}", notifier.getUsername());
                return;
            }
            log.info("Mail notificator enabled for {} and will be activated now", notifier.getUsername());
            GmailIncomingMailReader reader = new GmailIncomingMailReader(this.notifier, notifier, notifier.getTelegramGroupId(), defaultZoneId);
            executorService.submit(reader::startListening);
        }
    }
}
