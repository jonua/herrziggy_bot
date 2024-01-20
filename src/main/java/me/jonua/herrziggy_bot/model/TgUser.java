package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class TgUser extends BaseEntity {
    private String firstName;
    private String lastName;
    private String userId;
    private String username;
    private Boolean isBot;
    private Boolean isPremium;

    @OneToOne(fetch = FetchType.LAZY)
    private Calendar calendar;
}
