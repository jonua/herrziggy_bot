package me.jonua.herrziggy_bot.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
@ConditionalOnProperty(value = "bot.poll_new_mails", havingValue = "true")
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private GmailIncomingMailReader gmailIncomingMailReader;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        gmailIncomingMailReader.startListening();
    }
}
