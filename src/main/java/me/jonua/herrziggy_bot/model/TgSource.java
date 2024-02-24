package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import me.jonua.herrziggy_bot.enums.Gender;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@Entity
public class TgSource extends BaseEntity {
    private String sourceId;
    private String type;
    private String firstName;
    private String lastName;
    private String username;
    private String title;
    private Boolean isBot = false;
    private Boolean isPremium = false;
    private String migrateFromChatId;
    private boolean kicked = false;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @ManyToOne
    private CalendarConfiguration calendarConfiguration;

    public String getBeautifiedName() {
        String result = "";
        if (StringUtils.isNotEmpty(firstName)) {
            result += firstName;
        }
        if (StringUtils.isNotEmpty(lastName)) {
            result += " " + lastName;
        }
        if (StringUtils.isNotEmpty(username)) {
            result += " (@" + username + ")";
        }
        return result;
    }
}
