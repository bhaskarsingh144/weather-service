package com.mxfz.weatherservice.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * Configuration for web server (Tomcat) to use virtual threads for HTTP requests.
 * This is separate from application-level executors.
 */
@Configuration
@EnableAsync
public class WebServerConfig {

    /**
     * Configures Tomcat to use virtual threads for handling HTTP requests.
     * This allows handling thousands of concurrent HTTP requests efficiently.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    /**
     * Configures Spring's async task executor to use virtual threads.
     * This is used when @Async annotation is used in controllers/services.
     */
    @Bean(name = "applicationTaskExecutor")
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}

