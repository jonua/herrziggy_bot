package me.jonua.herrziggy_bot.data.jpa.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

@Getter
@AllArgsConstructor
public class TgSourceProjection {
    private Date date;
    private String type;
    private String firstName;
    private String lastName;
    private String username;
    private String title;

    public String beautify() {
        String result = "";
        if (StringUtils.isNotEmpty(type)) {
            result += "[" + type + "] ";
        }

        if (StringUtils.isNotEmpty(firstName)) {
            result += firstName;
        }
        if (StringUtils.isNotEmpty(lastName)) {
            result += " " + lastName;
        }
        if (StringUtils.isNotEmpty(username)) {
            result += " [" + username + "]";
        }
        if (StringUtils.isNotEmpty(title)) {
            result += " (" + title + ")";
        }
        return result;
    }
}
