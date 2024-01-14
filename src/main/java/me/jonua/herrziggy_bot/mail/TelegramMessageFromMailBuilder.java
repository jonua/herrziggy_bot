package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.HashTags;
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
    private final MailNotificationContext context;
    private final StringBuilder tgMessageBuilder = new StringBuilder();
    List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> messages = new ArrayList<>();
    private String hashTags;

    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildTelegramMessages(Message message) {
        try {
            hashTags = String.format("%s %s", HashTags.HASHTASG_MAIL, context.getHashTagMailSendDate());

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
            if (bodyPart.getSize() < context.getAttachmentSizeThresholdBytes()) {
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

    private SendMessage buildTgSendMessage(String stringMessage) throws IOException {
        String info = buildMessageInfo();
        String text = info + "\n" + divider(context) + "\n\n" + escapeForMarkdownV2IfEnabled(context, stringMessage) + "\n" + divider(context) + "\n\n" + escapeForMarkdownV2IfEnabled(context, hashTags);

        text = reduceMessageIfNeeds(text);

        return new SendMessage(
                context.getTelegramChatId(),
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
        String caption = info + "\n" + divider(context) + "\n" + escapeForMarkdownV2IfEnabled(context, hashTags);

        SendPhoto sendPhotoMessage = new SendPhoto(
                context.getTelegramChatId(),
                null,
                new InputFile(bodyPart.getInputStream(), name),
                caption,
                false,
                null,
                null,
                null,
                null,
                true,
                false,
                false
        );
        messages.add(sendPhotoMessage);
    }

    private void buildTgSendDocument(BodyPart bodyPart, String name) throws MessagingException, IOException {
        String info = buildMessageInfo();
        String caption = info + "\n" + divider(context) + "\n" + escapeForMarkdownV2IfEnabled(context, hashTags);

        SendDocument sendDocumentMessage = new SendDocument(
                context.getTelegramChatId(),
                null,
                new InputFile(bodyPart.getInputStream(), name),
                caption,
                false,
                null,
                null,
                null,
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
                        _from_: *%s*
                        _date_: *%s*
                        """, escapeForMarkdownV2IfEnabled(context, context.getFromAsString()),
                DateUtils.formatDate(context.getSentDate(), "MMM d, yyyy")
        );
    }

    private static String divider(MailNotificationContext context) {
        return escapeForMarkdownV2IfEnabled(context, "---");
    }

    public static String escapeForMarkdownV2IfEnabled(MailNotificationContext context, String text) {
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
            return text.substring(0, MAX_MESSAGE_LENGTH - 10) + escapeForMarkdownV2IfEnabled(context, " ...");
        }
        return text;
    }
}
