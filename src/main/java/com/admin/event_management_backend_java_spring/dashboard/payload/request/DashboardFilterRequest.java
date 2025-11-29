package com.admin.event_management_backend_java_spring.dashboard.payload.request;

import lombok.Data;
import java.util.Date;

@Data
public class DashboardFilterRequest {
    // Date range filter - có thể để null để lấy tất cả dữ liệu
    private Date startDate;
    private Date endDate;
    
    // Preset options để dễ sử dụng
    private DatePreset preset;
    
    public enum DatePreset {
        TODAY,          // Hôm nay
        LAST_7_DAYS,    // 7 ngày gần nhất
        LAST_30_DAYS,   // 30 ngày gần nhất (mặc định)
        LAST_90_DAYS,   // 90 ngày gần nhất 
        THIS_WEEK,      // Tuần này
        LAST_WEEK,      // Tuần trước
        THIS_MONTH,     // Tháng này
        LAST_MONTH,     // Tháng trước
        THIS_QUARTER,   // Quý này
        LAST_QUARTER,   // Quý trước
        THIS_YEAR,      // Năm nay
        LAST_YEAR,      // Năm trước
        ALL_TIME        // Tất cả thời gian
    }
    
    /**
     * Tính toán startDate và endDate dựa trên preset
     */
    public void calculateDatesFromPreset() {
        if (preset == null) {
            preset = DatePreset.LAST_30_DAYS; // Mặc định
        }
        
        Date now = new Date();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        Date endDateValue = cal.getTime();
        
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        switch (preset) {
            case TODAY:
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_7_DAYS:
                cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_30_DAYS:
                cal.add(java.util.Calendar.DAY_OF_MONTH, -30);
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case THIS_WEEK:
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_WEEK:
                cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
                Date lastWeekEnd = new Date(cal.getTime().getTime() - 1);
                cal.add(java.util.Calendar.DAY_OF_MONTH, -6);
                this.startDate = cal.getTime();
                this.endDate = lastWeekEnd;
                break;
                
            case THIS_MONTH:
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_MONTH:
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                Date lastMonthEnd = cal.getTime();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                this.startDate = cal.getTime();
                this.endDate = lastMonthEnd;
                break;
                
            case THIS_QUARTER:
                int currentQuarter = (cal.get(java.util.Calendar.MONTH) / 3);
                cal.set(java.util.Calendar.MONTH, currentQuarter * 3);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_QUARTER:
                int lastQuarter = (cal.get(java.util.Calendar.MONTH) / 3) - 1;
                if (lastQuarter < 0) {
                    lastQuarter = 3;
                    cal.add(java.util.Calendar.YEAR, -1);
                }
                cal.set(java.util.Calendar.MONTH, lastQuarter * 3);
                cal.add(java.util.Calendar.MONTH, 2);
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                Date lastQuarterEnd = cal.getTime();
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                this.startDate = cal.getTime();
                this.endDate = lastQuarterEnd;
                break;
                
            case THIS_YEAR:
                cal.set(java.util.Calendar.MONTH, 0);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                this.startDate = cal.getTime();
                this.endDate = endDateValue;
                break;
                
            case LAST_YEAR:
                cal.add(java.util.Calendar.YEAR, -1);
                cal.set(java.util.Calendar.MONTH, 11);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 31);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                Date lastYearEnd = cal.getTime();
                cal.set(java.util.Calendar.MONTH, 0);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                this.startDate = cal.getTime();
                this.endDate = lastYearEnd;
                break;
                
            case ALL_TIME:
            default:
                this.startDate = null;
                this.endDate = null;
                break;
        }
    }
    
    /**
     * Kiểm tra xem có filter theo date range không
     */
    public boolean hasDateFilter() {
        return startDate != null && endDate != null;
    }
}
