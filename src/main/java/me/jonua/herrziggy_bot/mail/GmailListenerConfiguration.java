package me.jonua.herrziggy_bot.mail;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.service.mail.MailConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private MailConfigurationService mailConfigurationService;

    @Autowired
    private TelegramGroupNotifier notifier;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for (MailConfiguration config : mailConfigurationService.getActiveConfigurations()) {
            log.info("Mail notificator enabled for {} and will be activated now", config.getUsername());
            GmailIncomingMailReader reader = new GmailIncomingMailReader(this.notifier, config);
            executorService.submit(reader::startListening);
        }
    }
}
