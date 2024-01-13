package me.jonua.herrziggy_bot.mail;

import com.sun.mail.util.BASE64DecoderStream;
import jakarta.activation.MimeTypeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    private String decodeMessageBase64DecoderStream(BASE64DecoderStream content) throws IOException {
        return decodeMessage(content);
    }

    private static String decodeMessage(InputStream message) throws IOException {
        String result = "";
        if (message instanceof ByteArrayInputStream bis) {
            byte[] data = new byte[bis.available()];
            bis.read(data);
            result = new String(data, StandardCharsets.UTF_8);
        } else if (message instanceof BASE64DecoderStream decodeStream) {
            // Read all the bytes from the array in an ever-growing buffer.
            // Didn't use any utils for minimum dependencies. Don't to this at home!
            byte[] data = new byte[1024];
            int count = decodeStream.read(data);
            int startPos = 0;
            while (count == 1024) {
                byte[] addBuffer = new byte[data.length + 1024];
                System.arraycopy(data, 0, addBuffer, 0, data.length);
                startPos = data.length;
                data = addBuffer;
                count = decodeStream.read(data, startPos, 1024);
            }
            int maxlen = data.length - 1024 + count;

            // convert already decoded data in byte array to String.
            result = new String(data, 0, maxlen, StandardCharsets.UTF_8);
        }
        return result;
    }
}
