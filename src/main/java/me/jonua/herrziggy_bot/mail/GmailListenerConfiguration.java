package me.jonua.herrziggy_bot.mail;

import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.service.StorageService;
import me.jonua.herrziggy_bot.service.mail.MailConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private MailConfigurationService mailConfigurationService;

    @Autowired
    private TelegramGroupNotifier notifier;

    @Autowired
    private StorageService storageService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<MailConfiguration> activeMailConfigurations = mailConfigurationService.getActiveConfigurations();

        for (MailConfiguration config : activeMailConfigurations) {
            Optional<TgSource> source = storageService.findBySourceId(config.getTgSource().getSourceId());
            String title = source.map(TgSource::getTitle).orElse(null);
            log.info("Mail notificator enabled for {} and will notify about new messages to {}:{}",
                    config.getUsername(), config.getTgSource().getSourceId(), title);
            GmailIncomingMailReader reader = new GmailIncomingMailReader(this.notifier, config, mailConfigurationService);
            executorService.submit(reader::startListening);
        }

        if (activeMailConfigurations.isEmpty()) {
            log.info("No active mail configurations found");
        }
    }
}
