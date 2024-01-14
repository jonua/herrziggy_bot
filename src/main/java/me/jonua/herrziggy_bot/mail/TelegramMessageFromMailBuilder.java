package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.DateUtils;
import me.jonua.herrziggy_bot.utils.Utils;
import org.jsoup.Jsoup;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class TelegramMessageFromMailBuilder extends MailMessageParser {
    public static final int MAX_MESSAGE_LENGTH = 4096;
    private final MailNotificationContext ctx;
    private final StringBuilder tgMessageBuilder = new StringBuilder();
    List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> messages = new ArrayList<>();
    private String hashTags;

    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildTelegramMessages(Message message) {
        try {
            hashTags = String.format("#mail %s", ctx.getHashTagMailSendDate());

            parse(message);

            String stringMessage = tgMessageBuilder.toString();

            messages.add(buildTgSendMessage(stringMessage));

            return messages;
        } catch (Exception e) {
            log.error("Unable to convert mail to telegram message: {}", e.getMessage(), e);
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
            if (bodyPart.getSize() < ctx.getAttachmentSizeThresholdBytes()) {
                String attachmentName = Optional.of(contentType).map(ContentType::getParameterList).map(list -> list.get("name")).orElse("image");
                log.info("Attachment with name {} discovered", attachmentName);

                if (contentType.getPrimaryType().equalsIgnoreCase("image")) {
                    buildTgSendPhoto(bodyPart, attachmentName);
                } else {
                    buildTgSendDocument(bodyPart, attachmentName);
                }
            }

            String text = buildAttachmentInfo(contentType, bodyPart.getSize());
            onTextPlain(text);
        } catch (MessagingException e) {
            log.error("Unable to process body part: {}", e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private SendMessage buildTgSendMessage(String stringMessage) {
        String tags = hashTags + " #message";
        String info = buildMessageInfo();
        String text = info + "\n" +
                divider(ctx) +
                "\n" + tgEscape(ctx, stringMessage) +
                "\n\n" + tgEscape(ctx, tags);

        text = "*New mail" + tgEscape(ctx, "!") + "*\n" + tgEscape(ctx,"\n") + text;
        text = reduceMessageIfNeeds(text);

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

    private void buildTgSendPhoto(BodyPart bodyPart, String name) throws IOException, MessagingException {
        String info = buildMessageInfo();
        String tags = hashTags + " " + "#attachment #photo";
        String caption = info + "\n" + divider(ctx) + "\n" + tgEscape(ctx, tags);

        SendPhoto sendPhotoMessage = new SendPhoto(
                ctx.getTelegramChatId(),
                null,
                new InputFile(bodyPart.getInputStream(), name),
                caption,
                false,
                null,
                null,
                ctx.getTelegramMessageParseMode(),
                null,
                true,
                false,
                false
        );
        messages.add(sendPhotoMessage);
    }

    private void buildTgSendDocument(BodyPart bodyPart, String name) throws MessagingException, IOException {
        String info = buildMessageInfo();
        String tags = hashTags + " " + "#attachment #document";
        String caption = info + "\n" + divider(ctx) + "\n" + tgEscape(ctx, tags);

        SendDocument sendDocumentMessage = new SendDocument(
                ctx.getTelegramChatId(),
                null,
                new InputFile(bodyPart.getInputStream(), name),
                caption,
                false,
                null,
                null,
                ctx.getTelegramMessageParseMode(),
                null,
                null,
                true,
                false,
                false
        );
        messages.add(sendDocumentMessage);
    }

    private String buildMessageInfo() {
        return String.format("""
                        *from*: _%s_
                        *date*: _%s_
                        """, tgEscape(ctx, ctx.getFromAsString()),
                DateUtils.formatDate(ctx.getSentDate(), "MMM d, yyyy HH:mm")
        );
    }

    private static String divider(MailNotificationContext context) {
        return tgEscape(context, "---");
    }

    public static String tgEscape(MailNotificationContext context, String text) {
        if (!ParseMode.MARKDOWNV2.equalsIgnoreCase(context.getTelegramMessageParseMode()) &&
                !ParseMode.MARKDOWN.equalsIgnoreCase(context.getTelegramMessageParseMode())) {
            return text;
        }

        List<Character> charsToBeEscaped = List.of('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!');
        for (Character ch : charsToBeEscaped) {
            text = text.replace(ch.toString(), "\\" + ch);
        }
        return text;
    }

    private String reduceMessageIfNeeds(String text) {
        if (text.length() > MAX_MESSAGE_LENGTH) {
            return text.substring(0, MAX_MESSAGE_LENGTH - 10) + tgEscape(ctx, " ...");
        }
        return text;
    }
}
