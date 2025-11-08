package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.ResourceMetrics;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Service to collect and monitor system resource metrics
 */
@Service
@Slf4j
public class ResourceMonitorService {

    private final ExecutorService taskExecutor;
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final GarbageCollectorMXBean gcBean;

    @Value("${resource.monitor.enabled:true}")
    private boolean monitoringEnabled;

    public ResourceMonitorService(@Qualifier("taskExecutor") ExecutorService taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        
        // Get the first GC bean (usually there's at least one)
        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.gcBean = gcBeans.isEmpty() ? null : gcBeans.get(0);
    }

    /**
     * Collects current resource metrics
     */
    public ResourceMetrics collectMetrics() {
        if (!monitoringEnabled) {
            return null;
        }

        try {
            ResourceMetrics.ResourceMetricsBuilder builder = ResourceMetrics.builder()
                    .timestamp(LocalDateTime.now());

            // Memory Metrics
            collectMemoryMetrics(builder);

            // GC Metrics
            collectGCMetrics(builder);

            // Thread Metrics
            collectThreadMetrics(builder);

            // CPU Metrics
            collectCPUMetrics(builder);

            // System Memory (if available)
            collectSystemMemoryMetrics(builder);

            // Thread Pool Metrics
            collectThreadPoolMetrics(builder);

            return builder.build();
        } catch (Exception e) {
            log.error("Error collecting resource metrics", e);
            return null;
        }
    }

    private void collectMemoryMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        builder.heapUsed(heapUsage.getUsed())
                .heapMax(heapUsage.getMax())
                .heapCommitted(heapUsage.getCommitted())
                .nonHeapUsed(nonHeapUsage.getUsed());

        if (heapUsage.getMax() > 0) {
            double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
            builder.heapUsagePercentage(heapUsagePercent);
        }
    }

    private void collectGCMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        if (gcBean != null) {
            builder.gcCollectionCount(gcBean.getCollectionCount())
                    .gcCollectionTime(gcBean.getCollectionTime());
            
            // Calculate last GC duration (simplified - in real scenario, track previous values)
            builder.lastGcDuration(gcBean.getCollectionTime());
        }
    }

    private void collectThreadMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        builder.threadCount(threadBean.getThreadCount())
                .peakThreadCount(threadBean.getPeakThreadCount())
                .daemonThreadCount(threadBean.getDaemonThreadCount());
    }

    private void collectCPUMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        try {
            double processCpuLoad = osBean.getProcessCpuLoad() * 100;
            double systemCpuLoad = osBean.getSystemCpuLoad() * 100;
            
            builder.processCpuLoad(processCpuLoad)
                    .systemCpuLoad(systemCpuLoad);
        } catch (Exception e) {
            log.warn("Could not collect CPU metrics", e);
        }
    }

    private void collectSystemMemoryMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        try {
            long totalMemory = osBean.getTotalPhysicalMemorySize();
            long freeMemory = osBean.getFreePhysicalMemorySize();
            long usedMemory = totalMemory - freeMemory;
            
            builder.systemMemoryUsed(usedMemory)
                    .systemMemoryTotal(totalMemory);
            
            if (totalMemory > 0) {
                double systemMemoryPercent = (double) usedMemory / totalMemory * 100;
                builder.systemMemoryUsagePercentage(systemMemoryPercent);
            }
        } catch (Exception e) {
            log.warn("Could not collect system memory metrics", e);
        }
    }

    private void collectThreadPoolMetrics(ResourceMetrics.ResourceMetricsBuilder builder) {
        if (taskExecutor instanceof ThreadPoolExecutor tpe) {
            // Traditional thread pool metrics
            builder.threadPoolActiveCount(tpe.getActiveCount())
                    .threadPoolQueueSize(tpe.getQueue().size())
                    .threadPoolPoolSize(tpe.getPoolSize())
                    .threadPoolCompletedTasks(tpe.getCompletedTaskCount());
        } else {
            // Virtual threads don't have traditional pool metrics
            // Virtual threads are managed by the JVM and scale automatically
            builder.threadPoolActiveCount(null)
                    .threadPoolQueueSize(null) // Virtual threads don't use queues
                    .threadPoolPoolSize(null) // Virtual threads don't have a fixed pool size
                    .threadPoolCompletedTasks(null);
        }
    }

    /**
     * Get current heap usage percentage
     */
    public double getCurrentHeapUsagePercentage() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        if (heapUsage.getMax() > 0) {
            return (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        }
        return 0.0;
    }

    /**
     * Get current thread count
     */
    public int getCurrentThreadCount() {
        return threadBean.getThreadCount();
    }

    /**
     * Get current CPU usage
     */
    public double getCurrentCpuUsage() {
        try {
            return osBean.getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }
}

