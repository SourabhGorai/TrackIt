package com.trackIt.sla_service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        simpleCacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("rules"),
                new ConcurrentMapCache("ruleById"),
                new ConcurrentMapCache("ruleByServiceId"),
                new ConcurrentMapCache("ruleByPriorityId")
        ));

        return simpleCacheManager;
    }

}
