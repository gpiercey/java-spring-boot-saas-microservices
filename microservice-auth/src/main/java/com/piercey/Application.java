package com.piercey;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

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

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // We need to swap the default object mapper so we can configure how enums are converted
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());

        final RestTemplate restTemplate = builder.build();
        restTemplate.getMessageConverters().removeIf(m -> m.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        restTemplate.getMessageConverters().add(converter);

        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}