package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.service.mail.MailConfigurationService;

import javax.mail.Message;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

@Slf4j
@RequiredArgsConstructor
public class GmailMessageCountAdapter extends MessageCountAdapter {
    private final TelegramGroupNotifier messageNotifier;
    private final MailConfiguration mailConfiguration;
    private final MailConfigurationService mailConfigurationService;

    @Override
    public void messagesAdded(MessageCountEvent event) {
        for (Message message : event.getMessages()) {
            try {
                messageNotifier.notifySubscribers(mailConfiguration, message);
                mailConfigurationService.updateLastUse(mailConfiguration.getUuid());
            } catch (Exception e) {
                log.error("MessageCountAdapter error: {}", e.getMessage(), e);
            }
        }
    }
}
