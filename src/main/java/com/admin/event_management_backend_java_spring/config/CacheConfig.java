package com.admin.event_management_backend_java_spring.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        // Using ConcurrentMapCacheManager as fallback when Redis is not available
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configure cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "dashboard", "reports", "analytics", "events", "users", 
            "departments", "audit", "user-sessions", 
            "points", "registrations"
        ));
        
        return cacheManager;
    }

    // Fallback cache manager for development (when Redis is not available)
    @Bean(name = "fallbackCacheManager")
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager("dashboard", "reports", "audit", "events", "users", "departments");
    }
} 