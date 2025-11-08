package com.mxfz.weatherservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model class to hold resource metrics for prediction analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceMetrics {
    private LocalDateTime timestamp;
    
    // JVM Memory Metrics
    private long heapUsed;
    private long heapMax;
    private long heapCommitted;
    private long nonHeapUsed;
    private double heapUsagePercentage;
    
    // GC Metrics
    private long gcCollectionCount;
    private long gcCollectionTime;
    private long lastGcDuration;
    
    // Thread Metrics
    private int threadCount;
    private int peakThreadCount;
    private int daemonThreadCount;
    
    // CPU Metrics
    private double processCpuLoad;
    private double systemCpuLoad;
    
    // System Memory (if available)
    private long systemMemoryUsed;
    private long systemMemoryTotal;
    private double systemMemoryUsagePercentage;
    
    // Application-specific metrics
    private Integer activeRequests;
    private Integer queuedTasks;
    private Long averageResponseTime;
    
    // Thread Pool Metrics
    private Integer threadPoolActiveCount;
    private Integer threadPoolQueueSize;
    private Integer threadPoolPoolSize;
    private Long threadPoolCompletedTasks;
}

