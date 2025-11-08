package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.ResourceMetrics;
import com.mxfz.weatherservice.model.ResourcePrediction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Scheduled service that continuously monitors and predicts resource exhaustion
 * Logs metrics every 10 seconds in a well-structured format
 * Uses Java 21 features: text blocks, pattern matching, switch expressions
 */
@Service
@Slf4j
public class ResourceMonitoringScheduler {

    private final ResourcePredictionService predictionService;
    private final ResourceMonitorService monitorService;
    private ResourcePrediction lastPrediction;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResourceMonitoringScheduler(
            ResourcePredictionService predictionService,
            ResourceMonitorService monitorService) {
        this.predictionService = predictionService;
        this.monitorService = monitorService;
    }

    /**
     * Run monitoring and log metrics every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    public void monitorAndLogMetrics() {
        try {
            ResourceMetrics metrics = monitorService.collectMetrics();
            ResourcePrediction prediction = predictionService.predict();
            lastPrediction = prediction;

            if (metrics != null) {
                logStructuredMetrics(metrics, prediction);
            }

            if (prediction != null) {

                double riskScore = prediction.getRiskScore();
                String formattedRisk = String.format("%.2f", riskScore);

                // Log warnings
                if (prediction.getWarnings() != null && !prediction.getWarnings().isEmpty()) {
                    log.warn("⚠️  Resource Warnings (Risk: {}): {}",
                            formattedRisk,
                            String.join("; ", prediction.getWarnings()));
                }

                // Log critical issues
                if (prediction.getCriticalIssues() != null && !prediction.getCriticalIssues().isEmpty()) {
                    log.error("🚨 CRITICAL Resource Issues (Risk: {}): {}",
                            formattedRisk,
                            String.join("; ", prediction.getCriticalIssues()));
                }

                // Log throttling requirement
                if (prediction.requiresThrottling()) {
                    log.error("⏱️ THROTTLING REQUIRED - Level: {}, Risk Score: {}",
                            prediction.getLevel(),
                            formattedRisk);
                }
            }


        } catch (Exception e) {
            log.error("Error during resource monitoring", e);
        }
    }

    /**
     * Logs metrics in a well-structured, indented format using Java 21 text blocks
     */
    private void logStructuredMetrics(ResourceMetrics metrics, ResourcePrediction prediction) {
        String logMessage = buildMetricsReport(metrics, prediction);
        log.info(logMessage);
    }

    /**
     * Builds the metrics report using Java 21 text blocks
     */
    private String buildMetricsReport(ResourceMetrics metrics, ResourcePrediction prediction) {
        return String.format("""
                        
                        ═══════════════════════════════════════════════════════════════
                        📊 RESOURCE METRICS REPORT
                        ═══════════════════════════════════════════════════════════════
                        ⏰ Timestamp: %s
                        🎯 Prediction Level: %s | Risk Score: %.2f
                        ───────────────────────────────────────────────────────────────
                        %s
                        ═══════════════════════════════════════════════════════════════
                        """,
                metrics.getTimestamp().format(TIME_FORMATTER),
                prediction.getLevel(),
                prediction.getRiskScore(),
                buildMetricsSections(metrics)
        );
    }

    /**
     * Builds all metric sections using Java 21 features
     */
    private String buildMetricsSections(ResourceMetrics metrics) {
        return String.join("\n",
                buildMemorySection(metrics),
                buildGCSection(metrics),
                buildThreadSection(metrics),
                buildCPUSection(metrics),
                buildSystemMemorySection(metrics),
                buildThreadPoolSection(metrics)
        );
    }

    private String buildMemorySection(ResourceMetrics metrics) {
        return String.format("""
                        💾 MEMORY METRICS
                           ├─ Heap Used:        %s
                           ├─ Heap Max:         %s
                           ├─ Heap Committed:   %s
                           ├─ Non-Heap Used:    %s
                           └─ Heap Usage:       %.2f%%
                        """,
                formatBytes(metrics.getHeapUsed()),
                formatBytes(metrics.getHeapMax()),
                formatBytes(metrics.getHeapCommitted()),
                formatBytes(metrics.getNonHeapUsed()),
                metrics.getHeapUsagePercentage()
        );
    }

    private String buildGCSection(ResourceMetrics metrics) {
        if (metrics.getGcCollectionCount() > 0) {
            return """
                    🗑️  GARBAGE COLLECTION
                       ├─ Collection Count: %d
                       ├─ Total GC Time:     %d ms
                       └─ Last GC Duration:  %d ms
                    """.formatted(
                    metrics.getGcCollectionCount(),
                    metrics.getGcCollectionTime(),
                    metrics.getLastGcDuration()
            );
        }
        return "";
    }

    private String buildThreadSection(ResourceMetrics metrics) {
        return """
                🧵 THREAD METRICS
                   ├─ Current Threads:  %d
                   ├─ Peak Threads:      %d
                   └─ Daemon Threads:    %d
                """.formatted(
                metrics.getThreadCount(),
                metrics.getPeakThreadCount(),
                metrics.getDaemonThreadCount()
        );
    }

    private String buildCPUSection(ResourceMetrics metrics) {
        return """
                ⚡ CPU METRICS
                   ├─ Process CPU:      %.2f%%
                   └─ System CPU:       %.2f%%
                """.formatted(
                metrics.getProcessCpuLoad(),
                metrics.getSystemCpuLoad()
        );
    }

    private String buildSystemMemorySection(ResourceMetrics metrics) {
        if (metrics.getSystemMemoryTotal() > 0) {
            return """
                    💿 SYSTEM MEMORY
                       ├─ Used:             %s
                       ├─ Total:            %s
                       └─ Usage:            %.2f%%
                    """.formatted(
                    formatBytes(metrics.getSystemMemoryUsed()),
                    formatBytes(metrics.getSystemMemoryTotal()),
                    metrics.getSystemMemoryUsagePercentage()
            );
        }
        return "";
    }

    private String buildThreadPoolSection(ResourceMetrics metrics) {
        return switch (metrics.getThreadPoolActiveCount()) {
            case null -> """
                    🔧 THREAD POOL METRICS
                       └─ Using Virtual Threads (no pool metrics available)
                    """;
            default -> """
                    🔧 THREAD POOL METRICS
                       ├─ Active Count:     %d
                       ├─ Queue Size:       %d
                       ├─ Pool Size:        %d
                       └─ Completed Tasks:  %d
                    """.formatted(
                    metrics.getThreadPoolActiveCount(),
                    metrics.getThreadPoolQueueSize(),
                    metrics.getThreadPoolPoolSize(),
                    metrics.getThreadPoolCompletedTasks()
            );
        };
    }

    /**
     * Formats bytes to human-readable format using Java 21 switch expressions
     */
    private String formatBytes(long bytes) {
        if (bytes == 0) {
            return "0 B";
        }
        int unit = (int) Math.floor(Math.log(bytes) / Math.log(1024));
        return switch (unit) {
            case 0 -> bytes + " B";
            case 1 -> String.format("%.2f KB", bytes / 1024.0);
            case 2 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0));
            default -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        };
    }

    /**
     * Get the last prediction result
     */
    public ResourcePrediction getLastPrediction() {
        return lastPrediction;
    }
}

