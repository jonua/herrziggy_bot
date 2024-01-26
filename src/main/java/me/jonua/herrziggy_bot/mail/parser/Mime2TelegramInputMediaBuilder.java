package me.jonua.herrziggy_bot.mail.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.mail.MailNotificationContext;
import me.jonua.herrziggy_bot.utils.DateTimeUtils;
import me.jonua.herrziggy_bot.utils.Utils;
import org.jsoup.Jsoup;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.*;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import java.io.IOException;
import java.util.*;

import static me.jonua.herrziggy_bot.utils.TelegramMessageUtils.tgEscape;

@Slf4j
@RequiredArgsConstructor
public class Mime2TelegramInputMediaBuilder extends MimeMessageParser {
    private static final int ATTACHMENT_SIZE_LIMIT_MB = 50;
    private final MailNotificationContext ctx;
    private final Locale locale;
    private final StringBuilder tgMessageBuilder = new StringBuilder();

    private final Map<String, List<InputMedia>> medias = new HashMap<>();

    public Pair<SendMessage, Map<String, List<InputMedia>>> parseAndBuild(Message message) {
        try {
            parse(message);

            String stringMessage = tgMessageBuilder.toString();

            SendMessage sendMessage = buildTgSendMessage(stringMessage, locale);

            return Pair.of(sendMessage, medias);
        } catch (Exception e) {
            log.error("Unable to convert mail to input medias: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onTextPlain(String content) {
        tgMessageBuilder
                .append("\n")
                .append(content);
    }

    @Override
    protected void onText(String content, ContentType contentType) {
        String text = Jsoup.parse(content).text();
        this.onTextPlain(text);
    }

    @Override
    protected void onBodyPart(BodyPart bodyPart, ContentType contentType) {
        try {
            if (bytesToMb(bodyPart.getSize()) < ATTACHMENT_SIZE_LIMIT_MB) {
                String attachmentName = Optional.of(contentType).map(ContentType::getParameterList).map(list -> list.get("name")).orElse("image");
                log.info("Attachment with name {} discovered with type {}", attachmentName, contentType);

                if (contentType.getPrimaryType().equalsIgnoreCase("image")) {
                    buildPhotoMedia(bodyPart, attachmentName, locale);
                } else if (contentType.getPrimaryType().equalsIgnoreCase("video")) {
                    buildVideoMedia(bodyPart, attachmentName, locale);
                } else if (contentType.getPrimaryType().equalsIgnoreCase("audio")) {
                    buildAudioMedia(bodyPart, attachmentName, locale);
                } else {
                    buildDocumentMedia(bodyPart, attachmentName, locale);
                }
            } else {
                log.warn("Attachment {} has size more then limit {}mb and can not be forward to a telegram group",
                        contentType, ATTACHMENT_SIZE_LIMIT_MB);
            }

            String text = buildAttachmentInfo(contentType, bodyPart.getSize());
            onTextPlain(text);
        } catch (
                MessagingException e) {
            log.error("Unable to process body part: {}", e.getMessage(), e);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int bytesToMb(int bytes) {
        return bytes / 1024 / 1024;
    }

    @Override
    protected void onMessage(Message message, ContentType contentType) {
        String text;
        try {
            text = buildAttachmentInfo(contentType, message.getSize());
        } catch (MessagingException e) {
            log.error("Unable to get attachment size: {}", e.getMessage(), e);
            text = buildAttachmentInfo(contentType, -1);
        }
        onTextPlain(text);
    }

    private static String buildAttachmentInfo(ContentType contentType, int attachmentSizeBytes) {
        String attachmentFileName = Optional.of(contentType)
                .map(ContentType::getParameterList).map(params -> params.get("name")).orElse("-unknown name-");
        String attachmentSize = "-";
        if (attachmentSizeBytes > 0) {
            attachmentSize = Utils.humanReadableByteCountBin(attachmentSizeBytes);
        }
        return String.format("\t\t%s [%s]", attachmentFileName, attachmentSize);
    }

    private SendMessage buildTgSendMessage(String stringMessage, Locale locale) {
        String info = buildMessageInfo(locale);
        String text = info + "\n" +
                divider(ctx) +
                "\n" + tgEscape(ctx.getTelegramMessageParseMode(), stringMessage);

        text = "*New mail" + tgEscape(ctx.getTelegramMessageParseMode(), "!") + "*\n" + tgEscape(ctx.getTelegramMessageParseMode(), "\n") + text;

        return new SendMessage(
                ctx.getTelegramChatId(),
                null,
                text,
                ParseMode.MARKDOWNV2,
                false,
                false,
                null,
                null,
                null,
                true,
                false
        );
    }

    private void buildPhotoMedia(BodyPart bodyPart, String name, Locale locale) throws IOException, MessagingException {
        String key = "photo";
        List<InputMedia> list = medias.getOrDefault(key, new ArrayList<>());
        list.add(
                InputMediaPhoto.builder()
                        .media("attach://" + name)
                        .mediaName(name)
                        .newMediaStream(bodyPart.getInputStream())
                        .isNewMedia(true)
                        .build()
        );
        medias.put(key, list);
    }

    private void buildVideoMedia(BodyPart bodyPart, String name, Locale locale) throws MessagingException, IOException {
        String key = "video";
        List<InputMedia> list = medias.getOrDefault(key, new ArrayList<>());
        list
                .add(
                        InputMediaVideo.builder()
                                .media("attach://" + name)
                                .mediaName(name)
                                .newMediaStream(bodyPart.getInputStream())
                                .isNewMedia(true)
                                .build()
                );
        medias.put(key, list);
    }

    private void buildAudioMedia(BodyPart bodyPart, String name, Locale locale) throws MessagingException, IOException {
        String key = "audio";
        List<InputMedia> list = medias.getOrDefault(key, new ArrayList<>());
        list.add(
                InputMediaAudio.builder()
                        .media("attach://" + name)
                        .mediaName(name)
                        .newMediaStream(bodyPart.getInputStream())
                        .isNewMedia(true)
                        .build()
        );
        medias.put(key, list);
    }

    private void buildDocumentMedia(BodyPart bodyPart, String name, Locale locale) throws MessagingException, IOException {
        String key = "other";
        List<InputMedia> list = medias.getOrDefault(key, new ArrayList<>());
        list
                .add(
                        InputMediaDocument.builder()
                                .media("attach://" + name)
                                .mediaName(name)
                                .newMediaStream(bodyPart.getInputStream())
                                .isNewMedia(true)
                                .build()
                );
        medias.put(key, list);
    }

    private String buildMessageInfo(Locale locale) {
        return String.format(
                """
                        *from*: _%s_
                        *subject*: _%s_
                        *date*: _%s_
                        """,
                tgEscape(ctx.getTelegramMessageParseMode(), ctx.getFromAsString()),
                tgEscape(ctx.getTelegramMessageParseMode(), ctx.getSubject()),
                tgEscape(ctx.getTelegramMessageParseMode(), DateTimeUtils.formatDate(ctx.getSentDate(), locale, DateTimeUtils.FORMAT_FULL_DATE_SHORT_TIME))
        );
    }

    private static String divider(MailNotificationContext context) {
        return tgEscape(context.getTelegramMessageParseMode(), "---");
    }
}
