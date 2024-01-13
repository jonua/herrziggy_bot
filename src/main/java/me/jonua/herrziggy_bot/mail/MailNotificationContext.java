package me.jonua.herrziggy_bot.mail;

import lombok.Getter;
import lombok.Setter;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Getter
public class MailNotificationContext {
    private List<Address> from;
    private String subject;
    private ZonedDateTime sentDate;
    @Setter
    private String telegramChatId;
    @Setter
    private ZoneId zoneId;
    @Setter
    private Integer attachmentSizeThresholdBytes;
    private Object hashTagMailSendDate;

    public static MailNotificationContext fromMessage(Message mailMessage, ZoneId zoneId) throws MessagingException {
        MailNotificationContext meta = new MailNotificationContext();
        meta.from = Arrays.asList(mailMessage.getFrom());
        meta.subject = mailMessage.getSubject();

        meta.sentDate = ZonedDateTime.ofInstant(mailMessage.getSentDate().toInstant(), zoneId);
        meta.hashTagMailSendDate = "#date_" + DateTimeFormatter.ofPattern("yyyy_MM_dd").format(meta.sentDate);

        return meta;
    }
}
