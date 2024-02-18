package me.jonua.herrziggy_bot.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.data.jpa.projections.TgSourceProjection;
import me.jonua.herrziggy_bot.data.jpa.repository.CalendarRepository;
import me.jonua.herrziggy_bot.enums.Gender;
import me.jonua.herrziggy_bot.model.CalendarConfiguration;
import me.jonua.herrziggy_bot.model.TgSource;
import me.jonua.herrziggy_bot.model.TgSourceRepository;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        String sourceIdString = Optional.ofNullable(tgUserId).map(String::valueOf).orElse(null);
        return findBySourceId(sourceIdString);
    }

    @Transactional
    public Optional<TgSource> findBySourceId(String tgUserId) {
        if (StringUtils.isEmpty(tgUserId)) {
            return Optional.empty();
        }

        return tgSourceRepository.findBySourceId(tgUserId);
    }

    @Transactional
    public Optional<CalendarConfiguration> findCalendarByUuid(String calendarUuid) {
        return calendarRepository.findByUuid(calendarUuid);
    }

    @Transactional
    public void assignCalendar(Long tgUserId, String calendarUuid) {
        findCalendarByUuid(calendarUuid)
                .ifPresent(calendarConfiguration -> {
                    findBySourceId(tgUserId).ifPresent(source -> {
                        source.setCalendarConfiguration(calendarConfiguration);
                        tgSourceRepository.save(source);
                        log.info("Calendar updated for source:{}. New calendar is {}", tgUserId, calendarConfiguration.getUuid());
                    });
                });
    }

    @Transactional
    public List<CalendarConfiguration> getCalendars(Sort sort) {
        return calendarRepository.findAll(sort);
    }

    @Transactional
    public void updateMigrateToChatId(String destinationChatId, String newSourceId) {
        log.info("Chat {} migrated to new id {}", destinationChatId, newSourceId);
        tgSourceRepository.updateMigrateToChatId(destinationChatId, newSourceId);
    }

    @Transactional
    public List<TgSource> findPrivateSources(Gender gender, Date updateDateLowerBoundary) {
        return tgSourceRepository.findPrivateSources(gender, updateDateLowerBoundary);
    }

    @Transactional
    public void markSourceAsKicked(Long sourceId) {
        tgSourceRepository.markAsKicked(sourceId);
    }

    @Transactional
    public void markSourceAsUnKicked(Long sourceId) {
        tgSourceRepository.markAsUnKicked(sourceId);
    }

    @Transactional
    public Map<Date, List<TgSourceProjection>> getStatNewUsers() {
        List<TgSourceProjection> stat = tgSourceRepository.getStatNewUsers(Date.from(ZonedDateTime.now().minusDays(7).toInstant()));
        return groupByDateAndSort(stat);
    }

    @Transactional
    public Map<Date, List<TgSourceProjection>> getStatActiveUsers() {
        List<TgSourceProjection> stat = tgSourceRepository.getStatActiveUsers(Date.from(ZonedDateTime.now().minusDays(7).toInstant()));
        return groupByDateAndSort(stat);
    }

    @NotNull
    private static Map<Date, List<TgSourceProjection>> groupByDateAndSort(List<TgSourceProjection> stat) {
        Map<Date, List<TgSourceProjection>> result = stat.stream().collect(
                Collectors.groupingBy(TgSourceProjection::getDate)
        );
        return result.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
