package me.jonua.herrziggy_bot.mail;

import jakarta.activation.MimeTypeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public abstract class MailMessageParser {
    protected void onTextPlain(String content) {
        log.info("onTextPlain");
    }

    protected void onText(String content, ContentType contentType) {
        log.info("onText");
    }

    protected void onBodyPart(BodyPart bodyPart, ContentType contentType) {
        log.info("onUnknownBodyPart");
    }

    protected void onMessage(Message message, ContentType contentType) {
        log.info("onUnknownMessage");
    }

    public void parse(Message message) throws MessagingException, IOException, MimeTypeParseException {
        ContentType contentType = new ContentType(message.getContentType());
        switch (contentType.getPrimaryType().toLowerCase()) {
            case "text":
                if (contentType.getSubType().equalsIgnoreCase("plain")) {
                    onTextPlain(message.getContent().toString());
                }
                break;
            case "multipart":
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                parseMimeMultipart(mimeMultipart);
                break;
            default:
                onMessage(message, contentType);
        }
    }

    private void parseMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException, MimeTypeParseException {
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                onTextPlain((String) bodyPart.getContent()); // without return, same text appears twice in my tests
            }
            log.debug("BodyPart {} in not a text/plain. Trying to parse...", bodyPart);
            parseBodyPart(bodyPart);
        }
    }

    private void parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException, MimeTypeParseException {
        ContentType contentType = new ContentType(bodyPart.getContentType());
        log.info("parsing {} ...", contentType);

        switch (contentType.getPrimaryType().toLowerCase()) {
            case "text":
                if (contentType.getSubType().equalsIgnoreCase("text")) {
                    onText(bodyPart.getContent().toString(), contentType);
                }
                break;
            case "multipart":
                parseMimeMultipart((MimeMultipart) bodyPart.getContent());
                break;
            default:
                onBodyPart(bodyPart, contentType);
        }
    }
}
