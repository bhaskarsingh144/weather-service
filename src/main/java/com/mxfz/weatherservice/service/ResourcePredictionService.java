package com.mxfz.weatherservice.service;

import com.mxfz.weatherservice.model.ResourceMetrics;
import com.mxfz.weatherservice.model.ResourcePrediction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to predict potential resource exhaustion and application crashes
 */
@Service
@Slf4j
public class ResourcePredictionService {

    private final ResourceMonitorService resourceMonitorService;
    
    // Thresholds for prediction (configurable via application.yml)
    @Value("${resource.prediction.heap.warning:70.0}")
    private double heapWarningThreshold;
    
    @Value("${resource.prediction.heap.critical:85.0}")
    private double heapCriticalThreshold;
    
    @Value("${resource.prediction.heap.imminent:95.0}")
    private double heapImminentThreshold;
    
    @Value("${resource.prediction.cpu.warning:70.0}")
    private double cpuWarningThreshold;
    
    @Value("${resource.prediction.cpu.critical:85.0}")
    private double cpuCriticalThreshold;
    
    @Value("${resource.prediction.thread.warning:80}")
    private int threadWarningThreshold;
    
    @Value("${resource.prediction.thread.critical:90}")
    private int threadCriticalThreshold;
    
    @Value("${resource.prediction.gc.duration.warning:1000}")
    private long gcDurationWarningThreshold;
    
    @Value("${resource.prediction.gc.duration.critical:5000}")
    private long gcDurationCriticalThreshold;
    
    @Value("${resource.prediction.queue.warning:50}")
    private int queueWarningThreshold;
    
    @Value("${resource.prediction.queue.critical:80}")
    private int queueCriticalThreshold;

    // Historical metrics for trend analysis
    private final List<ResourceMetrics> metricsHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    public ResourcePredictionService(ResourceMonitorService resourceMonitorService) {
        this.resourceMonitorService = resourceMonitorService;
    }

    /**
     * Analyzes current metrics and predicts potential issues
     */
    public ResourcePrediction predict() {
        ResourceMetrics currentMetrics = resourceMonitorService.collectMetrics();
        if (currentMetrics == null) {
            return ResourcePrediction.builder()
                    .timestamp(LocalDateTime.now())
                    .level(ResourcePrediction.PredictionLevel.SAFE)
                    .riskScore(0.0)
                    .warnings(new ArrayList<>())
                    .criticalIssues(new ArrayList<>())
                    .build();
        }

        // Add to history for trend analysis
        addToHistory(currentMetrics);

        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        // Analyze Heap Memory
        PredictionResult heapResult = analyzeHeapMemory(currentMetrics);
        warnings.addAll(heapResult.warnings());
        criticalIssues.addAll(heapResult.criticalIssues());
        riskScore = Math.max(riskScore, heapResult.riskScore());
        level = getHigherLevel(level, heapResult.level());

        // Analyze CPU
        PredictionResult cpuResult = analyzeCPU(currentMetrics);
        warnings.addAll(cpuResult.warnings());
        criticalIssues.addAll(cpuResult.criticalIssues());
        riskScore = Math.max(riskScore, cpuResult.riskScore());
        level = getHigherLevel(level, cpuResult.level());

        // Analyze Threads
        PredictionResult threadResult = analyzeThreads(currentMetrics);
        warnings.addAll(threadResult.warnings());
        criticalIssues.addAll(threadResult.criticalIssues());
        riskScore = Math.max(riskScore, threadResult.riskScore());
        level = getHigherLevel(level, threadResult.level());

        // Analyze GC
        PredictionResult gcResult = analyzeGC(currentMetrics);
        warnings.addAll(gcResult.warnings());
        criticalIssues.addAll(gcResult.criticalIssues());
        riskScore = Math.max(riskScore, gcResult.riskScore());
        level = getHigherLevel(level, gcResult.level());

        // Analyze Thread Pool
        PredictionResult threadPoolResult = analyzeThreadPool(currentMetrics);
        warnings.addAll(threadPoolResult.warnings());
        criticalIssues.addAll(threadPoolResult.criticalIssues());
        riskScore = Math.max(riskScore, threadPoolResult.riskScore());
        level = getHigherLevel(level, threadPoolResult.level());

        // Trend Analysis
        PredictionResult trendResult = analyzeTrends();
        warnings.addAll(trendResult.warnings());
        criticalIssues.addAll(trendResult.criticalIssues());
        riskScore = Math.max(riskScore, trendResult.riskScore());
        level = getHigherLevel(level, trendResult.level());

        // Project future metrics
        ResourceMetrics projectedMetrics = projectMetrics(currentMetrics);

        return ResourcePrediction.builder()
                .timestamp(LocalDateTime.now())
                .level(level)
                .riskScore(Math.min(riskScore, 1.0))
                .warnings(warnings)
                .criticalIssues(criticalIssues)
                .currentMetrics(currentMetrics)
                .projectedMetrics(projectedMetrics)
                .build();
    }

    private PredictionResult analyzeHeapMemory(ResourceMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        double heapUsage = metrics.getHeapUsagePercentage();

        if (heapUsage >= heapImminentThreshold) {
            criticalIssues.add(String.format("Heap memory at %.2f%% - IMMINENT CRASH RISK", heapUsage));
            riskScore = 0.95;
            level = ResourcePrediction.PredictionLevel.IMMINENT;
        } else if (heapUsage >= heapCriticalThreshold) {
            criticalIssues.add(String.format("Heap memory at %.2f%% - CRITICAL", heapUsage));
            riskScore = 0.75;
            level = ResourcePrediction.PredictionLevel.CRITICAL;
        } else if (heapUsage >= heapWarningThreshold) {
            warnings.add(String.format("Heap memory at %.2f%% - Approaching threshold", heapUsage));
            riskScore = 0.5;
            level = ResourcePrediction.PredictionLevel.WARNING;
        } else {
            riskScore = heapUsage / 100.0 * 0.3; // Scale to 0-0.3 for safe range
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private PredictionResult analyzeCPU(ResourceMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        double cpuUsage = metrics.getProcessCpuLoad();

        if (cpuUsage >= cpuCriticalThreshold) {
            criticalIssues.add(String.format("CPU usage at %.2f%% - High load may cause freezing", cpuUsage));
            riskScore = 0.7;
            level = ResourcePrediction.PredictionLevel.CRITICAL;
        } else if (cpuUsage >= cpuWarningThreshold) {
            warnings.add(String.format("CPU usage at %.2f%% - Monitoring", cpuUsage));
            riskScore = 0.4;
            level = ResourcePrediction.PredictionLevel.WARNING;
        } else {
            riskScore = cpuUsage / 100.0 * 0.2;
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private PredictionResult analyzeThreads(ResourceMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        int threadCount = metrics.getThreadCount();
        int peakThreads = metrics.getPeakThreadCount();

        if (threadCount >= threadCriticalThreshold) {
            criticalIssues.add(String.format("Thread count at %d - Thread exhaustion risk", threadCount));
            riskScore = 0.65;
            level = ResourcePrediction.PredictionLevel.CRITICAL;
        } else if (threadCount >= threadWarningThreshold) {
            warnings.add(String.format("Thread count at %d - High thread usage", threadCount));
            riskScore = 0.35;
            level = ResourcePrediction.PredictionLevel.WARNING;
        }

        if (peakThreads > threadCount * 1.5) {
            warnings.add(String.format("Peak threads (%d) significantly higher than current (%d)", peakThreads, threadCount));
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private PredictionResult analyzeGC(ResourceMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        if (metrics.getLastGcDuration() > 0) {
            long gcDuration = metrics.getLastGcDuration();
            
            if (gcDuration >= gcDurationCriticalThreshold) {
                criticalIssues.add(String.format("GC duration %d ms - Application may freeze during GC", gcDuration));
                riskScore = 0.6;
                level = ResourcePrediction.PredictionLevel.CRITICAL;
            } else if (gcDuration >= gcDurationWarningThreshold) {
                warnings.add(String.format("GC duration %d ms - Long GC pauses", gcDuration));
                riskScore = 0.3;
                level = ResourcePrediction.PredictionLevel.WARNING;
            }
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private PredictionResult analyzeThreadPool(ResourceMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        // Virtual threads don't have queue metrics - they scale automatically
        // Only analyze thread pool metrics if using traditional thread pools
        if (metrics.getThreadPoolQueueSize() != null && metrics.getThreadPoolQueueSize() > 0) {
            int queueSize = metrics.getThreadPoolQueueSize();
            
            if (queueSize >= queueCriticalThreshold) {
                criticalIssues.add(String.format("Thread pool queue at %d - Tasks backing up", queueSize));
                riskScore = 0.7;
                level = ResourcePrediction.PredictionLevel.CRITICAL;
            } else if (queueSize >= queueWarningThreshold) {
                warnings.add(String.format("Thread pool queue at %d - Queue growing", queueSize));
                riskScore = 0.4;
                level = ResourcePrediction.PredictionLevel.WARNING;
            }
        } else if (metrics.getThreadPoolQueueSize() == null) {
            // Virtual threads are in use - no queue to monitor
            // Monitor overall thread count instead (handled by analyzeThreads)
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private PredictionResult analyzeTrends() {
        List<String> warnings = new ArrayList<>();
        List<String> criticalIssues = new ArrayList<>();
        double riskScore = 0.0;
        ResourcePrediction.PredictionLevel level = ResourcePrediction.PredictionLevel.SAFE;

        if (metricsHistory.size() < 3) {
            return new PredictionResult(warnings, criticalIssues, riskScore, level);
        }

        // Analyze heap trend
        ResourceMetrics recent = metricsHistory.get(metricsHistory.size() - 1);
        ResourceMetrics older = metricsHistory.get(0);
        
        double heapTrend = recent.getHeapUsagePercentage() - older.getHeapUsagePercentage();
        
        if (heapTrend > 10) { // Heap growing by more than 10% in recent history
            warnings.add(String.format("Heap memory growing rapidly: +%.2f%% trend", heapTrend));
            riskScore = 0.4;
            level = ResourcePrediction.PredictionLevel.WARNING;
        }

        return new PredictionResult(warnings, criticalIssues, riskScore, level);
    }

    private ResourceMetrics projectMetrics(ResourceMetrics current) {
        // Simple linear projection based on current trends
        // In production, you might use more sophisticated algorithms
        
        if (metricsHistory.size() < 2) {
            return current;
        }

        ResourceMetrics recent = metricsHistory.get(metricsHistory.size() - 1);
        ResourceMetrics older = metricsHistory.get(metricsHistory.size() - 2);

        double heapGrowthRate = recent.getHeapUsagePercentage() - older.getHeapUsagePercentage();
        double projectedHeap = current.getHeapUsagePercentage() + heapGrowthRate;

        return ResourceMetrics.builder()
                .heapUsagePercentage(Math.min(projectedHeap, 100.0))
                .timestamp(LocalDateTime.now().plusMinutes(5)) // Project 5 minutes ahead
                .build();
    }

    private void addToHistory(ResourceMetrics metrics) {
        metricsHistory.add(metrics);
        if (metricsHistory.size() > MAX_HISTORY_SIZE) {
            metricsHistory.remove(0);
        }
    }

    private ResourcePrediction.PredictionLevel getHigherLevel(
            ResourcePrediction.PredictionLevel current,
            ResourcePrediction.PredictionLevel newLevel) {
        return newLevel.ordinal() > current.ordinal() ? newLevel : current;
    }

    private record PredictionResult(
            List<String> warnings,
            List<String> criticalIssues,
            double riskScore,
            ResourcePrediction.PredictionLevel level
    ) {}
}

