package me.jonua.herrziggy_bot.utils;

import me.jonua.herrziggy_bot.mail.MailNotificationContext;
import org.telegram.telegrambots.meta.api.methods.ParseMode;

import java.util.List;

public final class TelegramMessageUtils {
    private static final int MAX_MESSAGE_LENGTH = 4096;

    public static String tgEscape(String parseMode, String text) {
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
        if (text.length() > MAX_MESSAGE_LENGTH) {
            return text.substring(0, MAX_MESSAGE_LENGTH - 10) + tgEscape(parseMode, " ...");
        }
        return text;
    }
}
