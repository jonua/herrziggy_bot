package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.service.mail.MailConfigurationService;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.internet.InternetAddress;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GmailMessageCountAdapter extends MessageCountAdapter {
    private final TelegramGroupNotifier messageNotifier;
    private final MailConfiguration mailConfiguration;
    private final MailConfigurationService mailConfigurationService;
    private final static List<String> IGNORED_DOMAINS = List.of("accounts.google.com");

    @Override
    public void messagesAdded(MessageCountEvent event) {
        for (Message message : event.getMessages()) {
            try {
                if (isFiltered(message)) {
                    log.warn("The message from {} filtered: {}", message.getFrom(), message);
                    continue;
                }
                messageNotifier.notifySubscribers(mailConfiguration, message);
                mailConfigurationService.updateLastUse(mailConfiguration.getUuid());
            } catch (Exception e) {
                log.error("MessageCountAdapter error: {}", e.getMessage(), e);
            }
        }
    }

    private boolean isFiltered(Message message) {
        try {
            Address[] addresses = message.getFrom();
            String mailDomain = ((InternetAddress) addresses[0]).getAddress().split("@")[1];
            return IGNORED_DOMAINS.contains(mailDomain);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
