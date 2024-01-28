package me.jonua.herrziggy_bot.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.HashTags;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.service.StorageService;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAudio;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static me.jonua.herrziggy_bot.utils.TelegramMessageUtils.MAX_MESSAGE_LENGTH;
import static me.jonua.herrziggy_bot.utils.TelegramMessageUtils.reduceMessageIfNeeds;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramGroupNotifier {
    private final TelegramMessageBuilderService telegramMessageBuilder;
    private final MessageSender messageSender;
    private final StorageService storageService;

    public void notifySubscribers(MailConfiguration mailConfiguration, Message mailMessage) throws MessagingException {
        log.info("New mail from receiver {}", mailConfiguration.getUsername());
        for (TgSource tgSource : mailConfiguration.getTgSources()) {
            MailNotificationContext ctx = buildContext(mailMessage, mailConfiguration.getZoneId(), tgSource.getSourceId());

            Pair<SendMessage, Map<String, List<InputMedia>>> parsedResult = telegramMessageBuilder.buildFromMail(mailMessage, ctx);

            SendMessage messageToBeSend = provideTags(parsedResult.getFirst(), ctx);
            Map<String, List<InputMedia>> medias = parsedResult.getSecond();
            List<SendMediaGroup> groups = toGroups(tgSource.getSourceId(), medias);

            log.info("Mail for group {} parsed with {} medias will be sent to the chat", tgSource.getSourceId(), medias.size());
            sendToTelegram(messageToBeSend, groups);
            log.info("Mail for group {} parsed with {} medias send to the chat", tgSource.getSourceId(), medias.size());
        }
    }

    private void sendToTelegram(SendMessage messageToBeSend, List<SendMediaGroup> groups) {
        try {
            org.telegram.telegrambots.meta.api.objects.Message sendMessageResult = sendMessage(messageToBeSend.getChatId(), messageToBeSend, false)
                    .orElseThrow(() -> {
                        log.error("Unable to send message");
                        return new RuntimeException("Unable to send message");
                    });

            for (SendMediaGroup group : groups) {
                group.setReplyToMessageId(sendMessageResult.getMessageId());
                if (group.getMedias().size() == 1) {
                    sendAsSingleMedia(group, sendMessageResult);
                } else {
                    sendMessage(group.getChatId(), group, false);
                }
            }
        } catch (TelegramApiException e) {
            log.error("Unable to send message: {}", e.getMessage(), e);
        }
    }

    private void sendAsSingleMedia(SendMediaGroup group, org.telegram.telegrambots.meta.api.objects.Message replyTo) throws TelegramApiException {
        InputMedia media = group.getMedias().getFirst();
        final String mediaName = Optional.of(media).map(InputMedia::getCaption)
                .orElse(Optional.ofNullable(media.getMediaName())
                        .orElse("unknown name"));

        switch (media) {
            case InputMediaAudio ignoredAudio -> {
                SendAudio message = SendAudio.builder()
                        .chatId(group.getChatId())
                        .replyToMessageId(replyTo.getMessageId())
                        .audio(new InputFile(media.getNewMediaStream(), mediaName))
                        .build();
                sendMessage(group.getChatId(), message, false);
            }
            case InputMediaVideo ignoredVideo -> {
                SendVideo message = SendVideo.builder()
                        .chatId(group.getChatId())
                        .replyToMessageId(replyTo.getMessageId())
                        .video(new InputFile(media.getNewMediaStream(), mediaName))
                        .build();
                sendMessage(group.getChatId(), message, false);
            }
            case InputMediaPhoto ignoredPhoto -> {
                SendPhoto message = SendPhoto.builder()
                        .chatId(group.getChatId())
                        .replyToMessageId(replyTo.getMessageId())
                        .photo(new InputFile(media.getNewMediaStream(), mediaName))
                        .build();
                sendMessage(group.getChatId(), message, false);
            }
            default -> {
                SendDocument message = SendDocument.builder()
                        .chatId(group.getChatId())
                        .replyToMessageId(replyTo.getMessageId())
                        .document(new InputFile(media.getNewMediaStream(), mediaName))
                        .build();
                sendMessage(group.getChatId(), message, false);
            }
        }
    }

    @NotNull
    private static MailNotificationContext buildContext(Message mailMessage, ZoneId zoneId, String sourceId) throws
            MessagingException {
        MailNotificationContext ctx = MailNotificationContext.fromMessage(mailMessage, zoneId);
        ctx.setTelegramChatId(sourceId);
        ctx.setTelegramMessageParseMode(ParseMode.MARKDOWNV2);
        return ctx;
    }

    private List<SendMediaGroup> toGroups(String chatId, Map<String, List<InputMedia>> medias) {
        List<List<InputMedia>> mediaGroups = partByGroups(medias);
        return mapInputMediaToGroup(chatId, mediaGroups);
    }

    private List<List<InputMedia>> partByGroups(Map<String, List<InputMedia>> medias) {
        List<List<InputMedia>> result = new ArrayList<>();

        for (Map.Entry<String, List<InputMedia>> entry : medias.entrySet()) {
            List<List<InputMedia>> mediaGroups = new ArrayList<>();
            for (InputMedia media : entry.getValue()) {
                if (mediaGroups.isEmpty() || mediaGroups.size() % 10 == 0) {
                    mediaGroups.add(new ArrayList<>());
                }

                mediaGroups.getLast().add(media);
            }

            result.addAll(mediaGroups);
        }

        return result;
    }

    @NotNull
    private static List<SendMediaGroup> mapInputMediaToGroup(String chatId, List<List<InputMedia>> mediaGroups) {
        List<SendMediaGroup> groups = new ArrayList<>();
        for (List<InputMedia> mediaGroup : mediaGroups) {
            groups.add(
                    SendMediaGroup.builder()
                            .chatId(chatId)
                            .medias(mediaGroup)
                            .build()
            );
        }
        return groups;
    }

    @NotNull
    private static SendMessage provideTags(SendMessage message, MailNotificationContext ctx) {
        String messageToBeSendText = message.getText();
        String tagsString = TelegramMessageUtils.tgEscape(ctx.getTelegramMessageParseMode(),
                String.join(" ", ctx.getHashTagMailSendDate(), HashTags.MAIL.getTag())
        );
        String reducedMessageText = reduceMessageIfNeeds(ctx.getTelegramMessageParseMode(), messageToBeSendText,
                MAX_MESSAGE_LENGTH - tagsString.length() - 10);
        reducedMessageText += "\n\n" + tagsString;
        message.setText(reducedMessageText);
        return message;
    }

    private Optional<org.telegram.telegrambots.meta.api.objects.Message> sendMessage(String chatId, PartialBotApiMethod<org.telegram.telegrambots.meta.api.objects.Message> tgMessage,
                                                                                     boolean errorHandled) {
        try {
            switch (tgMessage) {
                case SendMessage message -> {
                    return Optional.of(send(chatId, message));
                }
                case SendPhoto message -> {
                    return Optional.of(send(chatId, message));
                }
                case SendDocument message -> {
                    return Optional.of(send(chatId, message));
                }
                case SendVideo message -> {
                    return Optional.of(send(chatId, message));
                }
                case SendAudio message -> {
                    return Optional.of(send(chatId, message));
                }
                default -> log.error("Can't send message: unsupported message type: {}", tgMessage.getClass());
            }
        } catch (Exception e) {
            Consumer<String> resender = newSourceId -> sendMessage(newSourceId, tgMessage, true);
            if (tryHandle(e, chatId, resender, errorHandled)) {
                log.warn("Exception handled: {}", e.getMessage());
                return Optional.empty();
            }

            log.error("Not handled exception: unable to send message {}: {}", tgMessage.getClass(), e.getMessage(), e);
        }
        return Optional.empty();
    }

    private void sendMessage(String chatId, SendMediaGroup tgMessage, boolean errorHandled) {
        try {
            messageSender.send(tgMessage);
        } catch (Exception e) {
            Consumer<String> resender = newSourceId -> sendMessage(newSourceId, tgMessage, true);
            if (tryHandle(e, chatId, resender, errorHandled)) {
                log.warn("Exception handled: {}", e.getMessage());
                return;
            }

            log.error("Not handled exception: unable to send message {}: {}", tgMessage.getClass(), e.getMessage(), e);
        }
    }

    private boolean tryHandle(Exception e, String destinationChatId, Consumer<String> applyWithNewSourceId, boolean alreadyHandled) {
        if (alreadyHandled) {
            log.error("The error already handled: {}", e.getMessage());
            throw new RuntimeException("Already handled error", e);
        }

        if (e instanceof TelegramApiRequestException) {
            if (((TelegramApiRequestException) e).getParameters() != null) {
                Long migrateToChatId = ((TelegramApiRequestException) e).getParameters().getMigrateToChatId();
                if (migrateToChatId != null) {
                    String newSourceId = String.valueOf(migrateToChatId);
                    storageService.updateMigrateToChatId(destinationChatId, newSourceId);
                    applyWithNewSourceId.accept(newSourceId);
                    return true;
                }
            }
        }
        String message = String.format("Unable to sent message to group %s: %s", destinationChatId, e.getMessage());
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    private org.telegram.telegrambots.meta.api.objects.Message send(String chatId, SendAudio message) throws TelegramApiException {
        log.info("The next audio message will be sent to:{} message {}: {}",
                chatId, message.getReplyMarkup(), message.getCaption());
        message.setChatId(chatId);
        return messageSender.send(message);
    }

    private org.telegram.telegrambots.meta.api.objects.Message send(String chatId, SendVideo message) throws TelegramApiException {
        log.info("The next video message will be sent to:{} message {}: {}",
                chatId, message.getReplyMarkup(), message.getCaption());
        message.setChatId(chatId);
        return messageSender.send(message);
    }

    private org.telegram.telegrambots.meta.api.objects.Message send(String chatId, SendDocument message) throws TelegramApiException {
        log.info("The next document message will be sent to:{} document {} ({}) with caption {}",
                chatId, message.getDocument().getAttachName(), message.getDocument().getMediaName(), message.getCaption());
        message.setChatId(chatId);
        return messageSender.send(message);
    }

    private org.telegram.telegrambots.meta.api.objects.Message send(String chatId, SendPhoto message) throws TelegramApiException {
        log.info("The next photo message will be sent to:{} photo {} ({}) with caption {}",
                chatId, message.getPhoto().getAttachName(), message.getPhoto().getMediaName(), message.getCaption());
        message.setChatId(chatId);
        return messageSender.send(message);
    }

    private org.telegram.telegrambots.meta.api.objects.Message send(String chatId, SendMessage message) throws TelegramApiException {
        log.info("The next message will be sent to:{} message {}: {}",
                chatId, message.getReplyMarkup(), message.getText());
        message.setChatId(chatId);
        return messageSender.send(message);
    }

}
