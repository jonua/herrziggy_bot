package me.jonua.herrziggy_bot.flow;

import me.jonua.herrziggy_bot.MessageSender;
import me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.time.ZoneId.systemDefault;
import static java.time.ZonedDateTime.ofInstant;
import static me.jonua.herrziggy_bot.utils.DateTimeUtils.FORMAT_SHORT_DATE;
import static me.jonua.herrziggy_bot.utils.DateTimeUtils.formatDate;

public abstract class BaseStatUsersUserFlow implements UserFlow {
    @Autowired
    private MessageSender messageSender;
    @Value("${bot.locale}")
    private String defaultLocale;
    @Value("${bot.admin-telegram-id}")
    private Long adminTgId;

    @Override
    public void perform(Update update) {
        perform(update, List.of());
    }

    public void respondWithStat(String callbackQueryId, Map<Date, List<TgSourceProjection>> stat) {
        if (stat.isEmpty()) {
            messageSender.sendAnswerCallback(callbackQueryId, "No new users found at the period", false);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Date, List<TgSourceProjection>> entry : stat.entrySet()) {
                String date = formatDate(ofInstant(entry.getKey().toInstant(), systemDefault()), Locale.of(defaultLocale), FORMAT_SHORT_DATE);
                sb.append(date + ", " + entry.getValue().size() + "\n");
                for (TgSourceProjection user : entry.getValue()) {
                    sb.append("\t" + user.beautify() + "\n");
                }
                sb.append("\n");
            }
            messageSender.sendSilently(adminTgId, sb.toString());
            messageSender.sendAnswerCallback(callbackQueryId, "ready", false);
        }
    }
}
