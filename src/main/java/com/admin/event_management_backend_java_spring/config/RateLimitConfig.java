package com.admin.event_management_backend_java_spring.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket authenticationBucket() {
        // 10 requests per minute for authentication endpoints (increased for large system)
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Bean
    public Bucket generalApiBucket() {
        // 200 requests per minute for general API endpoints (increased for large system)
        Bandwidth limit = Bandwidth.classic(200, Refill.greedy(200, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Bean
    public Bucket adminApiBucket() {
        // 100 requests per minute for admin API endpoints
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Bean
    public Bucket reportApiBucket() {
        // 20 requests per minute for report generation endpoints (increased)
        Bandwidth limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Bean
    public Bucket bulkOperationBucket() {
        // 10 requests per minute for bulk operations (increased)
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket dashboardBucket() {
        // 50 requests per minute for dashboard endpoints
        Bandwidth limit = Bandwidth.classic(50, Refill.greedy(50, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket userApiBucket() {
        // 150 requests per minute for user-related endpoints
        Bandwidth limit = Bandwidth.classic(150, Refill.greedy(150, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket eventApiBucket() {
        // 100 requests per minute for event-related endpoints
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket pointsApiBucket() {
        // 80 requests per minute for points-related endpoints
        Bandwidth limit = Bandwidth.classic(80, Refill.greedy(80, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
    
    @Bean
    public Bucket auditApiBucket() {
        // 60 requests per minute for audit-related endpoints
        Bandwidth limit = Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
} 