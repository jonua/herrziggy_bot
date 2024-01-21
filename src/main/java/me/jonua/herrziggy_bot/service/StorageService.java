package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.model.Calendar;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.model.TgSourceRepository;
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
    private final CalendarRepository calendarRepository;
    private final TgSourceRepository tgSourceRepository;

    @Transactional
    public void upsertSource(User source) {
        Optional<TgSource> foundSource = findBySourceId(source.getId());
        if (foundSource.isEmpty()) {
            TgSource newUser = new TgSource();
            patchSource(newUser, source);
        } else {
            TgSource existsUser = foundSource.get();
            patchSource(existsUser, source);
        }
    }

    @Transactional
    public void upsertSource(Chat source) {
        Optional<TgSource> foundGroup = tgSourceRepository.findBySourceId(String.valueOf(source.getId()));
        if (foundGroup.isEmpty()) {
            TgSource newUser = new TgSource();
            patchSource(newUser, source);
        } else {
            TgSource existsUser = foundGroup.get();
            patchSource(existsUser, source);
        }
    }

    private void patchSource(TgSource entity, User tgUser) {
        entity.setSourceId(String.valueOf(tgUser.getId()));
        entity.setFirstName(tgUser.getFirstName());
        entity.setLastName(tgUser.getLastName());
        entity.setUsername(tgUser.getUserName());
        entity.setIsBot(Boolean.TRUE.equals(tgUser.getIsBot()));
        entity.setIsPremium(Boolean.TRUE.equals(tgUser.getIsPremium()));

        entity.setUpdateDate(new Date());

        tgSourceRepository.save(entity);
    }

    private void patchSource(TgSource entity, Chat tgGroup) {
        entity.setTitle(tgGroup.getTitle());
        entity.setSourceId(String.valueOf(tgGroup.getId()));
        entity.setType(tgGroup.getType());
        entity.setFirstName(tgGroup.getFirstName());
        entity.setLastName(tgGroup.getLastName());
        entity.setUsername(tgGroup.getUserName());
        entity.setTitle(tgGroup.getTitle());
        entity.setIsBot(false);
        entity.setIsPremium(false);

        entity.setUpdateDate(new Date());

        tgSourceRepository.save(entity);
    }

    @Transactional
    public Optional<TgSource> findBySourceId(Long tgUserId) {
        return tgSourceRepository.findBySourceId(String.valueOf(tgUserId));
    }

    @Transactional
    public Optional<Calendar> findCalendarByUser(String tgUserId) {
        return calendarRepository.findBySourceId(tgUserId);
    }

    @Transactional
    public Optional<Calendar> findCalendarByUuid(String calendarUuid) {
        return calendarRepository.findByUuid(calendarUuid);
    }

    @Transactional
    public void assignCalendar(Long tgUserId, String calendarUuid) {
        findCalendarByUuid(calendarUuid)
                .ifPresent(calendar -> {
                    findBySourceId(tgUserId).ifPresent(source -> {
                        source.setCalendar(calendar);
                        tgSourceRepository.save(source);
                        log.info("Calendar updated for source:{}. New calendar is {}", tgUserId, calendar.getUuid());
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
