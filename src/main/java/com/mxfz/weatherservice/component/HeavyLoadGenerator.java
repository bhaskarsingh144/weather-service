package com.mxfz.weatherservice.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Generates heavy CPU + memory load at startup so that ResourceMonitorService
 * and ResourceMonitoringScheduler can show the impact.
 * 
 * You can easily swap between a normal pool and a virtual thread executor
 * using @Qualifier("taskExecutor") or @Qualifier("virtualTaskExecutor").
 */
@Service
@Slf4j
public class HeavyLoadGenerator {

    private final ExecutorService executor;

    public HeavyLoadGenerator(@Qualifier("taskExecutor") ExecutorService executor) {
        this.executor = executor;
    }
    /**
     * Runs at startup to simulate a heavy workload.
     */
//    @PostConstruct
    public void generateLoad() {
        log.info("🚀 Starting heavy load simulation using executor type: {}", executor.getClass().getSimpleName());

        int taskCount = 200;  // total tasks
        List<Callable<Long>> tasks = IntStream.range(0, taskCount)
                .mapToObj(i -> (Callable<Long>) () -> doHeavyComputation(i))
                .toList();

        try {
            long start = System.currentTimeMillis();
            List<Future<Long>> results = executor.invokeAll(tasks);
            long total = 0;
            for (Future<Long> f : results) {
                total += f.get();
            }
            long duration = System.currentTimeMillis() - start;
            log.info("✅ Heavy load simulation completed in {} ms, total result = {}", duration, total);
        } catch (Exception e) {
            log.error("Error during heavy load generation", e);
        }
    }

    /**
     * Simulates a CPU-intensive and memory-stressing computation.
     */
    private long doHeavyComputation(int index) {
        // Simulate CPU + memory load
        double sum = 0;
        int iterations = 5_000_000; // Increase to 50M for more load
        double[] data = new double[5000]; // small array to add GC pressure

        for (int i = 0; i < iterations; i++) {
            double val = Math.sin(i) * Math.cos(i / 2.0);
            data[i % data.length] = val;
            sum += val;
        }

        // Simple delay to simulate I/O blocking
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        return (long) (sum * 1_000);
    }
}
