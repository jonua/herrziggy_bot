package me.jonua.herrziggy_bot.mail;

import me.jonua.herrziggy_bot.TelegramGroupMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private GmailIncomingMailReader gmailIncomingMailReader;

    @Autowired
    private TelegramGroupMessageSender sender;
    @Value("${default-zone-id}")
    private String defaultZoneId;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        gmailIncomingMailReader.setMessageNotifier(new TelegramGroupNotifier(sender, defaultZoneId));
        gmailIncomingMailReader.startListening();
    }
}
