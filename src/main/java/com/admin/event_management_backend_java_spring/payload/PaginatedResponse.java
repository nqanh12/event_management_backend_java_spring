package com.admin.event_management_backend_java_spring.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> data;
    private PaginationInfo pagination;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
        private boolean isFirst;
        private boolean isLast;
    }
    
    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        PaginationInfo paginationInfo = new PaginationInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious(),
            page.isFirst(),
            page.isLast()
        );
        
        return new PaginatedResponse<>(page.getContent(), paginationInfo);
    }
} 