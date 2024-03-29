package me.jonua.herrziggy_bot.mail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.service.mail.MailConfigurationService;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Map;
import java.util.Properties;

@Slf4j
@RequiredArgsConstructor
public class GmailIncomingMailReader {
    private final TelegramGroupNotifier messageNotifier;
    private final MailConfiguration mailConfiguration;
    private final MailConfigurationService mailConfigurationService;

    public void startListening() {
        Properties properties = new Properties();
        properties.putAll(Map.of(
                "mail.debug", Boolean.valueOf(mailConfiguration.isDebug()).toString(),
                "mail.store.protocol", mailConfiguration.getStoreProtocol(),
                "mail.imaps.host", mailConfiguration.getImapsHost(),
                "mail.imaps.port", mailConfiguration.getImapsPort(),
                "mail.imaps.timeout", mailConfiguration.getImapsTimeout(),
                "mail.imap.partialfetch", "false",
                "mail.smtp.ssl.trust", "smtp.gmail.com",
                "mail.smtp.ssl.protocols", "SSLv3",
                "mail.smtp.starttls.required", "true",
                "mail.smtp.socketFactory.port", "587"
        ));

        Session session = Session.getInstance(properties); // not
        // getDefaultInstance
        IMAPStore store = null;
        Folder inbox = null;

        try {
            store = (IMAPStore) session.getStore("imaps");
            store.connect(mailConfiguration.getUsername(), mailConfiguration.getPassword());

            if (!store.hasCapability("IDLE")) {
                throw new RuntimeException("IDLE not supported");
            }

            inbox = store.getFolder("INBOX");
            inbox.addMessageCountListener(new GmailMessageCountAdapter(messageNotifier, mailConfiguration, mailConfigurationService));

            IdleThread idleThread = new IdleThread(inbox, mailConfiguration.getUsername(), mailConfiguration.getPassword());
            idleThread.setDaemon(false);
            idleThread.start();

            idleThread.join();
            // idleThread.kill(); //to terminate from another thread

        } catch (Exception e) {
            log.error("Mail listener error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            close(inbox);
            close(store);
        }
    }

    private static class IdleThread extends Thread {
        private final Folder folder;
        private final String username;
        private final String password;
        private volatile boolean running = true;

        public IdleThread(Folder folder, String username, String password) {
            super();
            this.folder = folder;
            this.username = username;
            this.password = password;
        }

        public synchronized void kill() {
            if (!running)
                return;
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    ensureOpen(folder, username, password);
                    log.trace("enter idle");
                    ((IMAPFolder) folder).idle();
                } catch (Exception e) {
                    log.error("IdleThread error {}", e.getMessage(), e);
                    // something went wrong
                    // wait and try again
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        log.error("IdleThread sleep interrupted: {}", e1.getMessage(), e1);
                    }
                }
            }
        }
    }

    public static void close(final Folder folder) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (final Exception e) {
            log.warn("Closing folder error {}", e.getMessage(), e);
        }
    }

    public static void close(final Store store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (final Exception e) {
            log.warn("Closing store error {}", e.getMessage(), e);
        }
    }

    public static void ensureOpen(final Folder folder, String username, String password) throws MessagingException {
        if (folder != null) {
            Store store = folder.getStore();
            if (store != null && !store.isConnected()) {
                store.connect(username, password);
            }
        } else {
            log.error("Unable to open a null folder");
            throw new MessagingException("Unable to open a null folder");
        }

        if (folder.exists() && !folder.isOpen() && (folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
            log.debug("open folder " + folder.getFullName());
            folder.open(Folder.READ_ONLY);
            if (!folder.isOpen()) {
                log.error("Unable to open folder {}", folder.getFullName());
                throw new MessagingException("Unable to open folder " + folder.getFullName());
            }
        }
    }
}
