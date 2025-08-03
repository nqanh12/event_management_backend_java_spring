package com.admin.event_management_backend_java_spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemMonitoringController {
    
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    
    /**
     * Get overall system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            health.put("uptime", formatUptime(runtimeBean.getUptime()));
            health.put("components", getBasicComponentsHealth());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(503).body(health);
        }
    }
    
    /**
     * Get detailed system metrics
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            metrics.put("memory", getMemoryMetrics());
            metrics.put("system", getSystemInfo());
            metrics.put("runtime", getRuntimeMetrics());
            metrics.put("database", getDatabaseMetrics());
            
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get memory usage information
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryInfo() {
        return ResponseEntity.ok(getMemoryMetrics());
    }
    
    /**
     * Get CPU and system information
     */
    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> getCpuInfo() {
        Map<String, Object> cpuInfo = new HashMap<>();
        
        try {
            cpuInfo.put("availableProcessors", osBean.getAvailableProcessors());
            cpuInfo.put("systemLoadAverage", osBean.getSystemLoadAverage());
            cpuInfo.put("osName", osBean.getName());
            cpuInfo.put("osVersion", osBean.getVersion());
            cpuInfo.put("osArch", osBean.getArch());
            
            // Try to get CPU usage if available
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsBean = 
                    (com.sun.management.OperatingSystemMXBean) osBean;
                cpuInfo.put("processCpuLoad", String.format("%.2f%%", 
                    sunOsBean.getProcessCpuLoad() * 100));
                cpuInfo.put("systemCpuLoad", String.format("%.2f%%", 
                    sunOsBean.getSystemCpuLoad() * 100));
            }
            
            return ResponseEntity.ok(cpuInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get application runtime information
     */
    @GetMapping("/runtime")
    public ResponseEntity<Map<String, Object>> getRuntimeInfo() {
        return ResponseEntity.ok(getRuntimeMetrics());
    }
    
    /**
     * Get disk space information
     */
    @GetMapping("/disk")
    public ResponseEntity<Map<String, Object>> getDiskInfo() {
        Map<String, Object> diskInfo = new HashMap<>();
        
        try {
            java.io.File root = new java.io.File("/");
            diskInfo.put("totalSpace", formatBytes(root.getTotalSpace()));
            diskInfo.put("freeSpace", formatBytes(root.getFreeSpace()));
            diskInfo.put("usableSpace", formatBytes(root.getUsableSpace()));
            diskInfo.put("usedSpace", formatBytes(root.getTotalSpace() - root.getFreeSpace()));
            
            double usagePercent = ((double)(root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace()) * 100;
            diskInfo.put("usagePercent", String.format("%.2f%%", usagePercent));
            
            return ResponseEntity.ok(diskInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Force garbage collection
     */
    @PostMapping("/gc")
    public ResponseEntity<Map<String, Object>> forceGarbageCollection() {
        Map<String, Object> before = getMemoryMetrics();
        
        System.gc();
        
        // Wait a bit for GC to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> after = getMemoryMetrics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("beforeGC", before);
        result.put("afterGC", after);
        result.put("gcTriggered", true);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get thread information
     */
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> getThreadInfo() {
        Map<String, Object> threadInfo = new HashMap<>();
        
        try {
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parentGroup;
            while ((parentGroup = rootGroup.getParent()) != null) {
                rootGroup = parentGroup;
            }
            
            threadInfo.put("activeThreads", rootGroup.activeCount());
            threadInfo.put("activeThreadGroups", rootGroup.activeGroupCount());
            
            // Get current thread info
            Thread currentThread = Thread.currentThread();
            threadInfo.put("currentThread", Map.of(
                "name", currentThread.getName(),
                "id", currentThread.getId(),
                "state", currentThread.getState().toString(),
                "priority", currentThread.getPriority(),
                "daemon", currentThread.isDaemon()
            ));
            
            return ResponseEntity.ok(threadInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get application properties
     */
    @GetMapping("/properties")
    public ResponseEntity<Map<String, Object>> getSystemProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        try {
            properties.put("javaVersion", System.getProperty("java.version"));
            properties.put("javaVendor", System.getProperty("java.vendor"));
            properties.put("javaHome", System.getProperty("java.home"));
            properties.put("osName", System.getProperty("os.name"));
            properties.put("osVersion", System.getProperty("os.version"));
            properties.put("userCountry", System.getProperty("user.country"));
            properties.put("userLanguage", System.getProperty("user.language"));
            properties.put("userTimezone", System.getProperty("user.timezone"));
            properties.put("fileEncoding", System.getProperty("file.encoding"));
            
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // Helper methods
    
    private Map<String, Object> getBasicComponentsHealth() {
        Map<String, Object> components = new HashMap<>();
        
        // Basic health checks without actuator
        components.put("application", Map.of("status", "UP"));
        components.put("database", Map.of("status", "UNKNOWN"));
        components.put("memory", Map.of("status", getMemoryStatus()));
        
        return components;
    }
    
    private String getMemoryStatus() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        double usagePercent = (double) heapUsed / heapMax * 100;
        
        if (usagePercent > 90) {
            return "DOWN";
        } else if (usagePercent > 75) {
            return "WARNING";
        } else {
            return "UP";
        }
    }
    
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> system = new HashMap<>();
        
        system.put("availableProcessors", osBean.getAvailableProcessors());
        system.put("systemLoadAverage", osBean.getSystemLoadAverage());
        system.put("osName", osBean.getName());
        system.put("osVersion", osBean.getVersion());
        system.put("osArch", osBean.getArch());
        
        return system;
    }
    
    private Map<String, Object> getMemoryMetrics() {
        Map<String, Object> memory = new HashMap<>();
        
        // Heap memory
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted();
        
        memory.put("heap", Map.of(
            "used", formatBytes(heapUsed),
            "max", formatBytes(heapMax),
            "committed", formatBytes(heapCommitted),
            "usagePercent", String.format("%.2f%%", (double) heapUsed / heapMax * 100)
        ));
        
        // Non-heap memory
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryBean.getNonHeapMemoryUsage().getMax();
        long nonHeapCommitted = memoryBean.getNonHeapMemoryUsage().getCommitted();
        
        memory.put("nonHeap", Map.of(
            "used", formatBytes(nonHeapUsed),
            "max", nonHeapMax > 0 ? formatBytes(nonHeapMax) : "undefined",
            "committed", formatBytes(nonHeapCommitted)
        ));
        
        return memory;
    }
    
    // Remove unused method
    // private Map<String, Object> getSystemMetrics() was removed to fix duplicate method error
    
    private Map<String, Object> getRuntimeMetrics() {
        Map<String, Object> runtime = new HashMap<>();
        
        runtime.put("vmName", runtimeBean.getVmName());
        runtime.put("vmVersion", runtimeBean.getVmVersion());
        runtime.put("vmVendor", runtimeBean.getVmVendor());
        runtime.put("startTime", LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(runtimeBean.getStartTime()),
            java.time.ZoneId.systemDefault()
        ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        runtime.put("uptime", formatUptime(runtimeBean.getUptime()));
        runtime.put("pid", runtimeBean.getName().split("@")[0]);
        
        return runtime;
    }
    
    private Map<String, Object> getDatabaseMetrics() {
        Map<String, Object> database = new HashMap<>();
        
        try {
            // This is a placeholder for database metrics
            // In a real application, you would inject your database connection
            // and get actual metrics like connection pool size, active connections, etc.
            database.put("status", "connected");
            database.put("type", "MongoDB");
            database.put("note", "Database metrics require actual connection");
        } catch (Exception e) {
            database.put("status", "error");
            database.put("error", e.getMessage());
        }
        
        return database;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", 
                days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}
