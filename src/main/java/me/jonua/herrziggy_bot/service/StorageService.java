package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.data.jpa.repository.TgUserRepository;
import me.jonua.herrziggy_bot.model.TgUser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final TgUserRepository tgUserRepository;

    @Transactional
    public void saveData(Update update) {
        Optional<Message> messageOpt = Optional.ofNullable(update).map(Update::getMessage);
        if (messageOpt.isEmpty()) {
            return;
        }

        User from = messageOpt.get().getFrom();

        Optional<TgUser> foundUser = tgUserRepository.findByUserId(String.valueOf(from.getId()));
        if (foundUser.isEmpty()) {
            TgUser newUser = new TgUser();
            patchUser(newUser, from);
        } else {
            TgUser existsUser = foundUser.get();
            patchUser(existsUser, from);
        }
    }

    private void patchUser(TgUser entity, User tgUser) {
        entity.setUserId(String.valueOf(tgUser.getId()));
        entity.setFirstName(tgUser.getFirstName());
        entity.setLastName(tgUser.getLastName());
        entity.setUsername(tgUser.getUserName());

        entity.setUpdateDate(new Date());

        tgUserRepository.save(entity);
    }
}
