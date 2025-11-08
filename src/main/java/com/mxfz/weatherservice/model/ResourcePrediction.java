package com.mxfz.weatherservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Prediction result indicating potential resource exhaustion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePrediction {
    private LocalDateTime timestamp;
    private PredictionLevel level;
    private double riskScore; // 0.0 to 1.0
    private List<String> warnings;
    private List<String> criticalIssues;
    private ResourceMetrics currentMetrics;
    private ResourceMetrics projectedMetrics; // Projected metrics in next time window
    
    public enum PredictionLevel {
        SAFE,           // No issues detected
        WARNING,        // Approaching thresholds
        CRITICAL,       // High risk of issues
        IMMINENT        // Crash/freeze likely soon
    }
    
    public boolean requiresThrottling() {
        return level == PredictionLevel.CRITICAL || level == PredictionLevel.IMMINENT;
    }
}

