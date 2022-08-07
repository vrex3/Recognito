package org.vrex.recognito.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {

    @Bean
    public Caffeine caffeine() {
        return Caffeine.newBuilder()
                .initialCapacity(ApplicationConstants.INITIAL_CAPACITY)
                .maximumSize(ApplicationConstants.MAX_CAPACITY)
                .expireAfterWrite(ApplicationConstants.EXPIRE_AFTER_WRITE_IN_MINUTES, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine caffeine) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager(
                ApplicationConstants.USER_PROFILE_CACHE,
                ApplicationConstants.APPLICATION_CACHE,
                ApplicationConstants.ROLE_RESOURCE_MAPPING_CACHE,
                ApplicationConstants.USER_CREDENTIALS_CACHE
        );
        caffeineCacheManager.setCaffeine(caffeine);
        return caffeineCacheManager;
    }
}
