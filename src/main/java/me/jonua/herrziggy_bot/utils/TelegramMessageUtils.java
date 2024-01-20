package me.jonua.herrziggy_bot.utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.objects.User;

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
}
