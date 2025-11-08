package com.mxfz.weatherservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExecutorConfig {

    @Bean(name = "taskExecutor")
    public ExecutorService normalThreadPool() {
        // Fixed thread pool (normal)
        return Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2
        );
    }

    @Bean(name = "virtualTaskExecutor")
    public ExecutorService virtualThreadPool() {
        // Virtual threads (Java 21 feature)
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
