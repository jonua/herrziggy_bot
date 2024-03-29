package me.jonua.herrziggy_bot.statistics;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;

//@Configuration
public class RedissonConfiguration {
    @Bean
    public RedissonClient buildRedissonClient() {
        Config config = new Config();
        config
                .useSingleServer()
                .setAddress("redis://localhost:6379");

        return Redisson.create(config);
    }
}
