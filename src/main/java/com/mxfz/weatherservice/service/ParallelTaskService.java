package com.mxfz.weatherservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Example service demonstrating how to use the ExecutorService for parallel operations.
 * This is a learning example for multi-threading.
 */
@Service
@Slf4j
public class ParallelTaskService {

    private final ExecutorService taskExecutor;

    public ParallelTaskService(@Qualifier("taskExecutor") ExecutorService taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Example: Execute multiple tasks in parallel
     * Each task simulates a long-running operation (5-30 seconds)
     *
     * @param numberOfTasks Number of tasks to execute in parallel
     * @return List of CompletableFuture results
     */
    public List<CompletableFuture<String>> executeParallelTasks(int numberOfTasks) {
        List<CompletableFuture<String>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfTasks; i++) {
            final int taskId = i + 1;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                return executeLongRunningTask(taskId);
            }, taskExecutor);

            futures.add(future);
        }

        return futures;
    }

    /**
     * Example: Execute tasks and wait for all to complete
     *
     * @param numberOfTasks Number of tasks to execute
     * @return List of results from all tasks
     */
    public List<String> executeAndWaitForAll(int numberOfTasks) {
        List<CompletableFuture<String>> futures = executeParallelTasks(numberOfTasks);

        // Wait for all tasks to complete and collect results
        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * Example: Execute tasks with timeout
     *
     * @param numberOfTasks Number of tasks to execute
     * @param timeoutSeconds Timeout in seconds
     * @return List of results (may be incomplete if timeout occurs)
     */
    public List<String> executeWithTimeout(int numberOfTasks, long timeoutSeconds) {
        List<CompletableFuture<String>> futures = executeParallelTasks(numberOfTasks);

        // Wait for all with timeout
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.get(timeoutSeconds, TimeUnit.SECONDS);
            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } catch (Exception e) {
            log.error("Timeout occurred while waiting for tasks to complete", e);
            return futures.stream()
                    .filter(CompletableFuture::isDone)
                    .map(CompletableFuture::join)
                    .toList();
        }
    }

    /**
     * Simulates a long-running task (5-30 seconds)
     * Replace this with your actual business logic
     *
     * @param taskId Task identifier
     * @return Result string
     */
    private String executeLongRunningTask(int taskId) {
        log.info("Task {} started on thread: {}", taskId, Thread.currentThread().getName());

        try {
            // Simulate work: random duration between 5-30 seconds
            long duration = 5000 + (long) (Math.random() * 25000);
            Thread.sleep(duration);

            String result = String.format("Task %d completed in %d ms", taskId, duration);
            log.info("Task {} completed on thread: {}", taskId, Thread.currentThread().getName());
            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Task {} was interrupted", taskId, e);
            return String.format("Task %d was interrupted", taskId);
        }
    }

    /**
     * Example: Process a list of items in parallel
     *
     * @param items List of items to process
     * @param processor Function to process each item
     * @param <T> Input type
     * @param <R> Result type
     * @return List of results
     */
    public <T, R> List<CompletableFuture<R>> processInParallel(
            List<T> items,
            java.util.function.Function<T, R> processor) {

        return items.stream()
                .map(item -> CompletableFuture.supplyAsync(
                        () -> processor.apply(item),
                        taskExecutor
                ))
                .toList();
    }

    /**
     * Get executor statistics (for monitoring/debugging)
     */
    public void logExecutorStats() {
        if (taskExecutor instanceof java.util.concurrent.ThreadPoolExecutor tpe) {
            log.info("Executor Stats - Active: {}, Pool: {}, Queue: {}, Completed: {}",
                    tpe.getActiveCount(),
                    tpe.getPoolSize(),
                    tpe.getQueue().size(),
                    tpe.getCompletedTaskCount());
        }
    }
}

