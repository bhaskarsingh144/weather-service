package com.mxfz.weatherservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Configuration for thread pool executor to handle parallel operations.
 * Suitable for long-running tasks (5-30 seconds).
 */
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Value("${executor.core-pool-size:5}")
    private int corePoolSize;

    @Value("${executor.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${executor.queue-capacity:100}")
    private int queueCapacity;

    @Value("${executor.keep-alive-seconds:60}")
    private long keepAliveSeconds;

    @Value("${executor.thread-name-prefix:weather-service-}")
    private String threadNamePrefix;

    /**
     * Creates a ThreadPoolExecutor bean for handling parallel operations.
     * This executor is configured for long-running tasks (5-30 seconds).
     *
     * @return ExecutorService configured for parallel task execution
     */
    @Bean(name = "taskExecutor")
    public ExecutorService taskExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,                    // Core pool size
                maxPoolSize,                     // Maximum pool size
                keepAliveSeconds,                // Keep alive time
                TimeUnit.SECONDS,                // Time unit
                new LinkedBlockingQueue<>(queueCapacity), // Work queue
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName(threadNamePrefix + thread.getId());
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );

        // Allow core threads to time out
        executor.allowCoreThreadTimeOut(false);

        return executor;
    }

    /**
     * Creates a simple cached thread pool executor for short-lived tasks.
     * This is useful for quick parallel operations.
     *
     * @return ExecutorService with cached thread pool
     */
    @Bean(name = "cachedExecutor")
    public ExecutorService cachedExecutor() {
        return Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setName("cached-executor-" + thread.getId());
            thread.setDaemon(false);
            return thread;
        });
    }

    /**
     * Creates a fixed thread pool executor with a specific number of threads.
     * Useful when you want to limit concurrent operations.
     *
     * @return ExecutorService with fixed thread pool
     */
    @Bean(name = "fixedExecutor")
    public ExecutorService fixedExecutor() {
        return Executors.newFixedThreadPool(
                maxPoolSize,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("fixed-executor-" + thread.getId());
                    thread.setDaemon(false);
                    return thread;
                }
        );
    }
}

