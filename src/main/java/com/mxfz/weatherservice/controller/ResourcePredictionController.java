package com.mxfz.weatherservice.controller;

import com.mxfz.weatherservice.model.ResourceMetrics;
import com.mxfz.weatherservice.model.ResourcePrediction;
import com.mxfz.weatherservice.service.ResourceMonitorService;
import com.mxfz.weatherservice.service.ResourceMonitoringScheduler;
import com.mxfz.weatherservice.service.ResourcePredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to expose resource prediction and monitoring endpoints
 */
@RestController
@RequestMapping("/api/resource")
@Slf4j
public class ResourcePredictionController {

    private final ResourcePredictionService predictionService;
    private final ResourceMonitorService monitorService;
    private final ResourceMonitoringScheduler monitoringScheduler;

    public ResourcePredictionController(
            ResourcePredictionService predictionService,
            ResourceMonitorService monitorService,
            ResourceMonitoringScheduler monitoringScheduler) {
        this.predictionService = predictionService;
        this.monitorService = monitorService;
        this.monitoringScheduler = monitoringScheduler;
    }

    /**
     * Get current resource prediction
     * GET /api/resource/prediction
     */
    @GetMapping("/prediction")
    public ResponseEntity<ResourcePrediction> getPrediction() {
        ResourcePrediction prediction = predictionService.predict();
        return ResponseEntity.ok(prediction);
    }

    /**
     * Get last cached prediction (from scheduled monitoring)
     * GET /api/resource/prediction/cached
     */
    @GetMapping("/prediction/cached")
    public ResponseEntity<ResourcePrediction> getCachedPrediction() {
        ResourcePrediction prediction = monitoringScheduler.getLastPrediction();
        if (prediction == null) {
            prediction = predictionService.predict();
        }
        return ResponseEntity.ok(prediction);
    }

    /**
     * Get current resource metrics
     * GET /api/resource/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<ResourceMetrics> getMetrics() {
        ResourceMetrics metrics = monitorService.collectMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Check if throttling is required
     * GET /api/resource/throttle-required
     */
    @GetMapping("/throttle-required")
    public ResponseEntity<ThrottleResponse> checkThrottleRequired() {
        ResourcePrediction prediction = predictionService.predict();
        return ResponseEntity.ok(new ThrottleResponse(
                prediction.requiresThrottling(),
                prediction.getLevel().toString(),
                prediction.getRiskScore(),
                prediction.getCriticalIssues()
        ));
    }

    /**
     * Get quick health check
     * GET /api/resource/health
     */
    @GetMapping("/health")
    public ResponseEntity<ResourceHealth> getHealth() {
        ResourceMetrics metrics = monitorService.collectMetrics();
        ResourcePrediction prediction = predictionService.predict();
        
        return ResponseEntity.ok(new ResourceHealth(
                prediction.getLevel().toString(),
                prediction.getRiskScore(),
                metrics != null ? metrics.getHeapUsagePercentage() : 0.0,
                metrics != null ? metrics.getProcessCpuLoad() : 0.0,
                metrics != null ? metrics.getThreadCount() : 0,
                prediction.getWarnings().size(),
                prediction.getCriticalIssues().size()
        ));
    }

    // Response DTOs
    public record ThrottleResponse(
            boolean throttleRequired,
            String level,
            double riskScore,
            java.util.List<String> criticalIssues
    ) {}

    public record ResourceHealth(
            String level,
            double riskScore,
            double heapUsagePercent,
            double cpuUsagePercent,
            int threadCount,
            int warningCount,
            int criticalIssueCount
    ) {}
}

