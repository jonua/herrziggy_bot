package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.utils.ResourcesUtils;
import org.jsoup.Jsoup;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class TelegramMessageFromMailBuilder extends MailMessageParser {
    private final String chatId;
    private final ZoneId zoneId;
    private final int attachmentSizeThresholdBytes;
    private final StringBuilder tgMessageBuilder = new StringBuilder();
    List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> messages = new ArrayList<>();

    public List<PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message>> buildTelegramMessages(Message message) {
        try {
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
            if (bodyPart.getSize() < attachmentSizeThresholdBytes) {
                String attachmentName = Optional.of(contentType).map(ContentType::getParameterList).map(list -> list.get("name")).orElse("image");
                log.info("Attachment with name {} discovered", attachmentName);

                if (contentType.getPrimaryType().equalsIgnoreCase("image")) {
                    buildTgSendPhoto(bodyPart, attachmentName);
                } else {
                    buildTgSendDocument(bodyPart, attachmentName);
                }
            } else {
                String text = String.format("\n/ attachment (?): [%s] /", contentType);
                onTextPlain(text);
            }
        } catch (MessagingException e) {
            log.error("Unable to process body part: {}", e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onMessage(Message message, ContentType contentType) {
        String text = String.format("\n/ attachment (?): [%s] /", contentType);
        onTextPlain(text);
    }

    private SendMessage buildTgSendMessage(String stringMessage) throws IOException {
        ZonedDateTime zdt = ZonedDateTime.now(zoneId);
        String date = DateTimeFormatter.ofPattern("yyyy_MM_dd").format(zdt);

        String readyMessage = ResourcesUtils.loadAsString("telegram-message-template.txt")
                .replace("{from}", "-")
                .replace("{subject}", "-")
                .replace("{body}", stringMessage)
                .replace("{hashtags}", "#mail #date_" + date);

        return new SendMessage(
                chatId,
                null,
                readyMessage,
                null,
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
        SendPhoto sendPhotoMessage = new SendPhoto(
                chatId,
                null,
                new InputFile(bodyPart.getInputStream(), name),
                name,
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
        SendDocument sendDocumentMessage = new SendDocument(
                chatId,
                null,
                new InputFile(bodyPart.getInputStream(), name),
                name,
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
}
