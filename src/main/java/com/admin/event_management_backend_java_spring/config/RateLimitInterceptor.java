package com.admin.event_management_backend_java_spring.config;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    @Qualifier("authenticationBucket")
    private Bucket authenticationBucket;

    @Autowired
    @Qualifier("generalApiBucket")
    private Bucket generalApiBucket;

    @Autowired
    @Qualifier("adminApiBucket")
    private Bucket adminApiBucket;

    @Autowired
    @Qualifier("reportApiBucket")
    private Bucket reportApiBucket;

    @Autowired
    @Qualifier("bulkOperationBucket")
    private Bucket bulkOperationBucket;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket = getBucketForRequest(requestURI, method);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }
    }

    private Bucket getBucketForRequest(String requestURI, String method) {
        // Authentication endpoints
        if (requestURI.startsWith("/api/auth") || requestURI.startsWith("/api/login")) {
            return authenticationBucket;
        }

        // Report generation endpoints
        if (requestURI.contains("/reports") || requestURI.contains("/export")) {
            return reportApiBucket;
        }

        // Bulk operation endpoints
        if (requestURI.startsWith("/api/async") || requestURI.contains("/bulk")) {
            return bulkOperationBucket;
        }

        // Admin endpoints
        if (requestURI.startsWith("/api/admin") || 
            requestURI.contains("/schools") || 
            requestURI.contains("/audit")) {
            return adminApiBucket;
        }

        // General API endpoints
        return generalApiBucket;
    }
} 