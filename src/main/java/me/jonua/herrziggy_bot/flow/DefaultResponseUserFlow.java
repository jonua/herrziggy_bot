package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.enums.flow.UserFlowType;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultResponseUserFlow implements UserFlow {
    @Value("${bot.admin-telegram-id}")
    private String botAdminUserId;

    @Value("${messages.menu-button-info-message}")
    private String botCommonInfoMessage;

    private final MessageSender messageSender;

    @Override
    public void perform(Update update) {
        perform(update, Map.of());
    }

    @Override
    public void perform(Update update, Map<String, Object> params) {
        Optional.of(update).map(Update::getMessage).map(Message::getFrom)
                .ifPresent(from -> {
                    forwardMessageToBotAdmin(update);
                    respondWithIntoToUser(from, update);
                });
    }

    private void respondWithIntoToUser(User from, Update update) {
        List<String> commands = new ArrayList<>();
        Arrays.stream(BotCommand.values()).filter(BotCommand::isIncludeInBotMenu)
                .forEach(command -> commands.add("/" + command.getCommand() + " - " + command.getDescription()));

        String message = botCommonInfoMessage + "\n\n";
        message += String.join("\n", commands);
        messageSender.sendSilently(from.getId(), message, update.getMessage().getMessageId());
    }

    private void forwardMessageToBotAdmin(Update update) {
        Message message = update.getMessage();
        String userInput = message.getText();
        if (message.hasSticker()) {
            userInput = "<sticker>";
        }

        String userInfo = TelegramMessageUtils.extractUserInfo(message.getFrom());
        String messageFullText = "#user_direct_message\nNew message from " + userInfo + ": " + userInput;
        InlineKeyboardMarkup keyboardMarkup = TelegramMessageUtils.buildInlineKeyboardMarkup(
                List.of(
                        new TelegramMessageUtils.KeyboardButton("Respond to " + userInfo,
                                UserFlowType.DIRECT_MESSAGE_FROM_ADMIN_TO_USER_FLOW.getCommandPrefix() + ":" + message.getFrom().getId()
                        )
                ),
                1
        );
        try {
            messageSender.send(messageFullText, keyboardMarkup, Long.parseLong(botAdminUserId));
        } catch (TelegramApiException e) {
            log.error("Unable to send message with markup to admin: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            SendSticker sendSticker = new SendSticker(
                    String.valueOf(message.getFrom().getId()),
                    new InputFile(sticker.getFileId())
            );
            messageSender.sendSilently(sendSticker);
        }
    }

    @Override
    public boolean isSupport(UserFlowType userFlowType) {
        return false;
    }
}
