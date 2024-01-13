package me.jonua.herrziggy_bot.mail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailIncomingMailReader {
    @Value("${gmail.username}")
    private String username;
    @Value("${gmail.apppassword}")
    private String password;
    @Value("${sourcemail.store_protocol}")
    private String mailStoreProtocol;
    @Value("${sourcemail.imaps.host}")
    private String mailImapsHost;
    @Value("${sourcemail.imaps.port}")
    private String mailImapsPort;
    @Value("${sourcemail.imaps.timeout}")
    private String mailImapsTimeout;

    @Setter
    private MessageNotifier messageNotifier;

    @Value("${mail.debug:false}")
    private boolean mailDebug;

    public void startListening() {

        Properties properties = new Properties();
        properties.put("mail.debug", Boolean.valueOf(mailDebug).toString());
        properties.put("mail.store.protocol", mailStoreProtocol);
        properties.put("mail.imaps.host", mailImapsHost);
        properties.put("mail.imaps.port", mailImapsPort);
        properties.put("mail.imaps.timeout", mailImapsTimeout);
        properties.put("mail.imap.partialfetch", "false");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.smtp.ssl.protocols", "SSLv3");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.socketFactory.port", "587");


        Session session = Session.getInstance(properties); // not
        // getDefaultInstance
        IMAPStore store = null;
        Folder inbox = null;

        try {
            store = (IMAPStore) session.getStore("imaps");
            store.connect(username, password);

            if (!store.hasCapability("IDLE")) {
                throw new RuntimeException("IDLE not supported");
            }

            inbox = store.getFolder("INBOX");
            inbox.addMessageCountListener(messageCountListener(messageNotifier, session));

            IdleThread idleThread = new IdleThread(inbox, username, password);
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

    private static MessageCountAdapter messageCountListener(MessageNotifier messageNotifier, Session session) {
        return new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent event) {
                for (Message message : event.getMessages()) {
                    try {
                        messageNotifier.notifySubscribers(message, session);
                    } catch (Exception e) {
                        log.error("MessageCountAdapter error: {}", e.getMessage(), e);
                    }
                }
            }
        };
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
                    System.out.println("enter idle");
                    ((IMAPFolder) folder).idle();
                } catch (Exception e) {
                    log.warn("IdleThread error {}", e.getMessage(), e);
                    // something went wrong
                    // wait and try again
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        log.warn("IdleThread sleep interrupted: {}", e1.getMessage(), e1);
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
            System.out.println("open folder " + folder.getFullName());
            folder.open(Folder.READ_ONLY);
            if (!folder.isOpen()) {
                log.error("Unable to open folder {}", folder.getFullName());
                throw new MessagingException("Unable to open folder " + folder.getFullName());
            }
        }
    }
}
