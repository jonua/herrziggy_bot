package me.jonua.herrziggy_bot.utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public final class TelegramMessageUtils {
    public static final int MAX_MESSAGE_LENGTH = 4096;

    public static String tgEscape(String parseMode, String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }

        if (!ParseMode.MARKDOWNV2.equals(parseMode) &&
                !ParseMode.MARKDOWN.equals(parseMode)) {
            return text;
        }

        List<Character> charsToBeEscaped = List.of('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!');
        for (Character ch : charsToBeEscaped) {
            text = text.replace(ch.toString(), "\\" + ch);
        }
        return text;
    }

    public static String reduceMessageIfNeeds(String parseMode, String text) {
        return reduceMessageIfNeeds(parseMode, text, MAX_MESSAGE_LENGTH - 10);
    }

    public static String reduceMessageIfNeeds(String parseMode, String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + tgEscape(parseMode, " ...");
        }
        return text;
    }

    public static String extractUserInfo(User from) {
        if (from == null) {
            return "-unknown user-";
        }

        String result = "";
        if (StringUtils.isNotEmpty(from.getFirstName())) {
            if (StringUtils.isNotEmpty(from.getLastName())) {
                result = String.format("%s %s", from.getFirstName(), from.getLastName());
            } else {
                result = from.getFirstName();
            }
        }

        if (!StringUtils.isEmpty(from.getUserName())) {
            if (StringUtils.isNotEmpty(result)) {
                result = "@" + from.getUserName() + " (" + result + ")";
            } else {
                result = "@" + from.getUserName();
            }
        }

        result += " [" + from.getId() + "]";

        return result;
    }

    public static InlineKeyboardMarkup buildInlineKeyboardMarkup(List<KeyboardButton> keyboardButtons, int columnsCount) {
        if (columnsCount < 1) {
            columnsCount = 1;
        }
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        for (KeyboardButton keyboardButton : keyboardButtons) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .callbackData(keyboardButton.buttonCallbackData())
                    .text(keyboardButton.buttonName())
                    .build();

            if (buttons.isEmpty() || buttons.getLast().size() % columnsCount == 0) {
                buttons.add(new ArrayList<>());
            }

            buttons.getLast().add(button);
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();
    }

    public record KeyboardButton(String buttonName, String buttonCallbackData) {
    }
}
