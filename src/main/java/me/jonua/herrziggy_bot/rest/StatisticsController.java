package me.jonua.herrziggy_bot.rest;

import lombok.RequiredArgsConstructor;
import me.jonua.herrziggy_bot.command.BotCommand;
import me.jonua.herrziggy_bot.utils.DateTimeUtils;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

//@RestController
@RequiredArgsConstructor
@RequestMapping("rest/statistics/commands")
public class StatisticsController {
    private final RedissonClient redissonClient;

    @GetMapping
    @Cacheable(cacheNames = "statistics")
    public ResponseEntity<Object> getStatistics() {
        ZonedDateTime endOfDay = DateTimeUtils.getEndOfDay(ZonedDateTime.now());
        ZonedDateTime startOfDay = DateTimeUtils.getStartOfDay(endOfDay);


        Map<String, Map<String, Long>> result = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            ZonedDateTime dStart = startOfDay.minusDays(i);
            ZonedDateTime dEnd = endOfDay.minusDays(i);

            Map<String, Long> commandStat = new HashMap<>();
            for (BotCommand command : BotCommand.values()) {
                long sum = redissonClient.getTimeSeries(command.getCommand(), new LongCodec())
                        .range(dStart.toEpochSecond(), dEnd.toEpochSecond())
                        .stream()
                        .mapToLong(l -> (Long) l)
                        .sum();
                commandStat.put(command.getCommand(), sum);
            }

            result.put(
                    DateTimeUtils.formatDate(dStart, Locale.of("en"), DateTimeUtils.FORMAT_SHORT_DATE_WITH_DAY_NAME),
                    commandStat
            );
        }

        return ResponseEntity.ok(result);
    }
}
