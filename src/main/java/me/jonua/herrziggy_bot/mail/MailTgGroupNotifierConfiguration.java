package me.jonua.herrziggy_bot.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(value = "bot.tg-group-incoming-mail-notifier")
public class MailTgGroupNotifierConfiguration {
    private List<Notifier> notifiers = new ArrayList<>();

    @Getter
    @Setter
    public static class Notifier {
        private Boolean enabled = false;
        private String username;
        private String password;
        private String storeProtocol;
        private boolean debug;
        private Imaps imaps;

        private String telegramGroupId;

        @Getter
        @Setter
        public static class Imaps {
            private String host;
            private String port;
            private String timeout;
        }
    }
}
