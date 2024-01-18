package me.jonua.herrziggy_bot.model;

import jakarta.persistence.Entity;
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
}
