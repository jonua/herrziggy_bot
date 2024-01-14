package me.jonua.herrziggy_bot.mail;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.ParseMode;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class MailNotificationContext {
    private List<Address> from;
    private String fromAsString;

    private String subject;

    private ZonedDateTime sentDate;
    private String hashTagMailSendDate;

    @Setter
    private String telegramChatId;

    @Setter
    private ZoneId zoneId;

    @Setter
    private Integer attachmentSizeThresholdBytes;
    @Setter
    private String telegramMessageParseMode = null;

    public static MailNotificationContext fromMessage(Message mailMessage, ZoneId zoneId) throws MessagingException {
        MailNotificationContext meta = new MailNotificationContext();
        meta.from = Arrays.asList(mailMessage.getFrom());
        meta.fromAsString = Stream.of(meta.from).map(Object::toString).collect(Collectors.joining(", "));

        meta.subject = mailMessage.getSubject();

        meta.sentDate = ZonedDateTime.ofInstant(mailMessage.getSentDate().toInstant(), zoneId);
        meta.hashTagMailSendDate = "#date" + DateTimeFormatter.ofPattern("yyyy_MM_dd").format(meta.sentDate);

        return meta;
    }
}
