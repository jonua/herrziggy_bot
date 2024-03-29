package me.jonua.herrziggy_bot.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.enums.AdminCommandOptions;
import me.jonua.herrziggy_bot.utils.TelegramMessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCommandService {
    @Value("${bot.admin-telegram-id}")
    private String adminTelegramId;
    private final MessageSender messageSender;

    public boolean isBotAdmin(Long fromId) {
        return Optional.ofNullable(fromId).map(String::valueOf).filter(adminTelegramId::equalsIgnoreCase).isPresent();
    }

    public void handle(Message message, Long fromId) {
        if (isBotAdmin(fromId)) {
            List<TelegramMessageUtils.KeyboardButton> kbButtons = Arrays.stream(AdminCommandOptions.values())
                    .map(option -> new TelegramMessageUtils.KeyboardButton(
                            option.getCommandName(),
                            option.getUserFlowType().getCommandPrefix()
                    )).toList();

            InlineKeyboardMarkup keyboard = TelegramMessageUtils.buildInlineKeyboardMarkup(kbButtons, 1);

            try {
                messageSender.send("Select admin command:", keyboard, adminTelegramId);
            } catch (TelegramApiException e) {
                log.error("Unable to send admin commands list: {}", e.getMessage(), e);
            }
        }
    }

    public boolean isAdminCommand(Message message) {
        return Optional.of(message).map(Message::getText).filter("admin"::equalsIgnoreCase).isPresent();
    }
}
