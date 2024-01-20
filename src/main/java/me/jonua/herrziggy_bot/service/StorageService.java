package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.data.jpa.repository.TgUserRepository;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.model.TgSourceRepository;
import me.jonua.herrziggy_bot.model.TgUser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final TgUserRepository tgUserRepository;
    private final CalendarRepository calendarRepository;
    private final TgSourceRepository tgSourceRepository;

    @Transactional
    public void upsertSource(Update update) {
        Optional<Message> messageOpt = Optional.ofNullable(update).map(Update::getMessage);
        if (messageOpt.isEmpty()) {
            return;
        }

        upsertSourceUser(messageOpt.get());
        upsertSourceChat(messageOpt.get());
    }

    private void upsertSourceUser(Message message) {
        User from = message.getFrom();
        if (from == null) {
            return;
        }

        Optional<TgUser> foundUser = tgUserRepository.findByUserId(String.valueOf(from.getId()));
        if (foundUser.isEmpty()) {
            TgUser newUser = new TgUser();
            patchUser(newUser, from);
        } else {
            TgUser existsUser = foundUser.get();
            patchUser(existsUser, from);
        }
    }

    private void upsertSourceChat(Message message) {
        Chat chat = message.getChat();
        if (chat == null) {
            return;
        }

        Optional<TgSource> foundGroup = tgSourceRepository.findBySourceId(String.valueOf(chat.getId()));
        if (foundGroup.isEmpty()) {
            TgSource newUser = new TgSource();
            patchSource(newUser, chat);
        } else {
            TgSource existsUser = foundGroup.get();
            patchSource(existsUser, chat);
        }
    }

    private void patchUser(TgUser entity, User tgUser) {
        entity.setUserId(String.valueOf(tgUser.getId()));
        entity.setFirstName(tgUser.getFirstName());
        entity.setLastName(tgUser.getLastName());
        entity.setUsername(tgUser.getUserName());
        entity.setIsBot(tgUser.getIsBot());
        entity.setIsPremium(tgUser.getIsPremium());

        entity.setUpdateDate(new Date());

        tgUserRepository.save(entity);
    }

    private void patchSource(TgSource entity, Chat tgGroup) {
        entity.setTitle(tgGroup.getTitle());
        entity.setSourceId(String.valueOf(tgGroup.getId()));
        entity.setType(tgGroup.getType());
        entity.setFirstName(tgGroup.getFirstName());
        entity.setLastName(tgGroup.getLastName());
        entity.setUsername(tgGroup.getUserName());
        entity.setTitle(tgGroup.getTitle());

        entity.setUpdateDate(new Date());

        tgSourceRepository.save(entity);
    }

    @Transactional
    public Optional<Calendar> findCalendar(String tgUserId) {
        return calendarRepository.findByUserId(tgUserId);
    }
}
