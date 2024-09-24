package com.piercey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.lang.NonNull;

import java.time.Duration;

@SpringBootApplication
@EnableMongoRepositories
@EnableCaching
public class Application {

    @Value("${spring.application.name}")
    private String applicationName;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RedisCacheManager cacheManager(final @NonNull RedisConnectionFactory connectionFactory) {
        final RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(getClass().getPackageName() + "." + applicationName + ".")
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}