package me.jonua.herrziggy_bot.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

@Configuration
public class GmailListenerConfiguration implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private GmailIncomingMailReader gmailIncomingMailReader;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        gmailIncomingMailReader.startListening();
    }
}
