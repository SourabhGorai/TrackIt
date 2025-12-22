package com.trackIt.independent_services.config;

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
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                // Role caches
                new ConcurrentMapCache("roles"),

                // Company caches
                new ConcurrentMapCache("allCompanies"),
                new ConcurrentMapCache("activeCompanies"),
                new ConcurrentMapCache("deletedCompanies"),

                // Service caches
                new ConcurrentMapCache("services"),
                new ConcurrentMapCache("servicesPublic"),
                new ConcurrentMapCache("servicesById"),
                new ConcurrentMapCache("servicesByClient"),
                new ConcurrentMapCache("servicesByProvider"),
                new ConcurrentMapCache("servicesByName"),

                // Priority caches
                new ConcurrentMapCache("priorities")
        ));
        return cacheManager;
    }
}