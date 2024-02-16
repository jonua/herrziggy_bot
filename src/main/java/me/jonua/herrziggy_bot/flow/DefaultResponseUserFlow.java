package me.jonua.herrziggy_bot.flow;

import lombok.RequiredArgsConstructor;
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
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DefaultResponseUserFlow implements UserFlow {
    @Value("${bot.feedback.sent-to-user-id}")
    private String botAdminUserId;

    @Value("${messages.menu-button-info-message}")
    private String botCommonInfoMessage;

    private final MessageSender messageSender;

    @Override
    public void perform(Update update) {
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
        String userInput = update.getMessage().getText();
        if (update.getMessage().hasSticker()) {
            userInput = "<sticker>";
        }

        String messageFullText = "#user_direct_message\nNew message from " + TelegramMessageUtils.extractUserInfo(update.getMessage().getFrom()) + ": " + userInput;
        messageSender.sendSilently(Long.parseLong(botAdminUserId), messageFullText);

        if (update.getMessage().hasSticker()) {
            Sticker sticker = update.getMessage().getSticker();
            SendSticker sendSticker = new SendSticker(
                    String.valueOf(update.getMessage().getFrom().getId()),
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
