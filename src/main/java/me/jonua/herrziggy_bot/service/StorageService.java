package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.data.jpa.repository.TgUserRepository;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.model.TgSourceRepository;
import me.jonua.herrziggy_bot.model.TgUser;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final TgUserRepository tgUserRepository;
    private final CalendarRepository calendarRepository;
    private final TgSourceRepository tgSourceRepository;

    @Transactional
    public void upsertSourceUser(User sourceUser) {
        Optional<TgUser> foundUser = findUserByTgId(sourceUser.getId());
        if (foundUser.isEmpty()) {
            TgUser newUser = new TgUser();
            patchUser(newUser, sourceUser);
        } else {
            TgUser existsUser = foundUser.get();
            patchUser(existsUser, sourceUser);
        }
    }

    @Transactional
    public void upsertSourceChat(Chat sourceChat) {
        Optional<TgSource> foundGroup = tgSourceRepository.findBySourceId(String.valueOf(sourceChat.getId()));
        if (foundGroup.isEmpty()) {
            TgSource newUser = new TgSource();
            patchSource(newUser, sourceChat);
        } else {
            TgSource existsUser = foundGroup.get();
            patchSource(existsUser, sourceChat);
        }
    }

    private void patchUser(TgUser entity, User tgUser) {
        entity.setUserId(String.valueOf(tgUser.getId()));
        entity.setFirstName(tgUser.getFirstName());
        entity.setLastName(tgUser.getLastName());
        entity.setUsername(tgUser.getUserName());
        entity.setIsBot(Boolean.TRUE.equals(tgUser.getIsBot()));
        entity.setIsPremium(Boolean.TRUE.equals(tgUser.getIsPremium()));

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
    public Optional<TgUser> findUserByTgId(Long tgUserId) {
        return tgUserRepository.findByUserId(String.valueOf(tgUserId));
    }

    @Transactional
    public Optional<Calendar> findCalendarByUser(String tgUserId) {
        return calendarRepository.findByUserId(tgUserId);
    }

    @Transactional
    public Optional<Calendar> findCalendarByUuid(String calendarUuid) {
        return calendarRepository.findByUuid(calendarUuid);
    }

    @Transactional
    public void assignCalendar(Long tgUserId, String calendarUuid) {
        findCalendarByUuid(calendarUuid)
                .ifPresent(calendar -> {
                    findUserByTgId(tgUserId).ifPresent(user -> {
                        user.setCalendar(calendar);
                        tgUserRepository.save(user);
                        log.info("Calendar updated for user:{}. New calendar is {}", tgUserId, calendar.getUuid());
                    });
                });
    }

    @Transactional
    public List<Calendar> getCalendars(Sort sort) {
        return calendarRepository.findAll(sort);
    }

    @Transactional
    public Optional<Calendar> findCalendarByGroup(String groupId) {
        return calendarRepository.findBySourceId(groupId);
    }
}
