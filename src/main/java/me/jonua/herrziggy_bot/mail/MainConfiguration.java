package me.jonua.herrziggy_bot.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("mail")
public class MainConfiguration {
    private String username;
    private String password;
    private String storeProtocol;
    private boolean debug;
    private Imaps imaps;

    @Getter
    @Setter
    public static class Imaps {
        private String host;
        private String port;
        private String timeout;
    }
}
