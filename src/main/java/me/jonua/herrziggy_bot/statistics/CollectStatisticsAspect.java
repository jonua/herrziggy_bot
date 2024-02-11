package me.jonua.herrziggy_bot.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jonua.herrziggy_bot.command.BotCommand;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.redisson.api.RTimeSeries;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.LongCodec;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CollectStatisticsAspect {
    private final RedissonClient redissonClient;

    @Pointcut("execution(public void me.jonua.herrziggy_bot.command.handlers.CommandHandlerService.handleCommand(..))")
    public void defaultPointcut() {
        // empty
    }

    @Around("defaultPointcut()")
    public Object around(ProceedingJoinPoint pjp) {
        try {
            Object[] args = pjp.getArgs();
            if (args != null && args.length > 0) {
                Object command = args[0];
                if (command instanceof BotCommand botCommand) {
                    RTimeSeries<Object> ts = redissonClient.getTimeSeries(botCommand.getCommand(), new LongCodec());
                    ts.add(ZonedDateTime.now().toEpochSecond(), 1, 30, TimeUnit.DAYS);
                }
            }
            return pjp.proceed();
        } catch (Throwable e) {
            log.error("Can't proceed pjp: {}", e.getMessage(), e);
            return null;
        }
    }
}
