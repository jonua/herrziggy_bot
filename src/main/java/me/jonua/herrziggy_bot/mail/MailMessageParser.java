package me.jonua.herrziggy_bot.mail;

import com.sun.mail.util.BASE64DecoderStream;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

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
@Service
@RequiredArgsConstructor
public class MailMessageParser {
    public String getTextFromMessage(Message message) throws MessagingException, IOException, MimeTypeParseException {
        ContentType contentType = new ContentType(message.getContentType());
        switch (contentType.getPrimaryType().toLowerCase()) {
            case "text":
                if (contentType.getSubType().equalsIgnoreCase("plain")) {
                    return message.getContent().toString();
                }
                break;
            case "multipart":
                MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
                return getTextFromMimeMultipart(mimeMultipart);
        }
        return unknownType(contentType);
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException, MimeTypeParseException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                return result + "\n" + bodyPart.getContent(); // without return, same text appears twice in my tests
            }
            log.debug("BodyPart {} in not a text/plain. Trying to parse...", bodyPart);
            result.append(parseBodyPart(bodyPart));
        }
        return result.toString();
    }

    private String parseBodyPart(BodyPart bodyPart) throws MessagingException, IOException, MimeTypeParseException {
        ContentType contentType = new ContentType(bodyPart.getContentType());
        log.info("parsing {} ...", contentType);

        switch (contentType.getPrimaryType().toLowerCase()) {
            case "text":
                if (contentType.getSubType().equalsIgnoreCase("text")) {
                    return "\n" + Jsoup.parse(bodyPart.getContent().toString()).text();
                }
                break;
            case "multipart":
                return getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
            case "image":
            case "audio":
//                return decodeMessageBase64DecoderStream((BASE64DecoderStream) bodyPart.getContent());
                return String.format("\n/ attachment (?): [%s] /", contentType);
        }

        return unknownType(contentType);
    }

    private String unknownType(ContentType type) {
        log.warn("Unknown content type: {}", type);
        return String.format("\n/ attachment (?): [%s] /", type);
    }

    private String unknownMimeType(MimeType type) {
        log.warn("Unknown mime type: {}", type);
        return String.format("\n/ attachment (?): [%s] /", type);
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
