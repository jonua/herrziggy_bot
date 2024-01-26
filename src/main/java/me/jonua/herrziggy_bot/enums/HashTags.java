package me.jonua.herrziggy_bot.enums;

import lombok.Getter;

@Getter
public enum HashTags {
    ATTACHMENT("attachment"),
    MAIL("mail"),
    ;

    private final String tagName;

    public String getTag() {
        return "#" + getTagName();
    }

    HashTags(String tagName) {
        this.tagName = tagName;
    }
}
