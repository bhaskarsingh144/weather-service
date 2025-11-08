package com.mxfz.weatherservice.controller;

import com.mxfz.weatherservice.service.ParallelTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for testing and learning multi-threading functionality.
 */
@RestController
@RequestMapping("/api/parallel")
@Slf4j
public class ParallelTaskController {

    private final ParallelTaskService parallelTaskService;

    public ParallelTaskController(ParallelTaskService parallelTaskService) {
        this.parallelTaskService = parallelTaskService;
    }

    /**
     * Execute multiple tasks in parallel
     * GET /api/parallel/execute?tasks=5
     *
     * @param tasks Number of tasks to execute (default: 5)
     * @return Response with task status
     */
    @GetMapping("/execute")
    public ResponseEntity<String> executeTasks(@RequestParam(defaultValue = "5") int tasks) {
        log.info("Executing {} tasks in parallel", tasks);
        parallelTaskService.logExecutorStats();

        List<CompletableFuture<String>> futures = parallelTaskService.executeParallelTasks(tasks);

        return ResponseEntity.ok(String.format(
                "Started %d tasks in parallel. Check logs for progress. " +
                "Use /api/parallel/status to check executor stats.",
                tasks
        ));
    }

    /**
     * Execute tasks and wait for all to complete
     * GET /api/parallel/execute-and-wait?tasks=5
     *
     * @param tasks Number of tasks to execute (default: 5)
     * @return List of task results
     */
    @GetMapping("/execute-and-wait")
    public ResponseEntity<List<String>> executeAndWait(@RequestParam(defaultValue = "5") int tasks) {
        log.info("Executing {} tasks and waiting for completion", tasks);
        parallelTaskService.logExecutorStats();

        List<String> results = parallelTaskService.executeAndWaitForAll(tasks);

        log.info("All {} tasks completed", tasks);
        parallelTaskService.logExecutorStats();

        return ResponseEntity.ok(results);
    }

    /**
     * Execute tasks with a timeout
     * GET /api/parallel/execute-with-timeout?tasks=10&timeout=30
     *
     * @param tasks Number of tasks to execute (default: 10)
     * @param timeout Timeout in seconds (default: 30)
     * @return List of completed task results
     */
    @GetMapping("/execute-with-timeout")
    public ResponseEntity<List<String>> executeWithTimeout(
            @RequestParam(defaultValue = "10") int tasks,
            @RequestParam(defaultValue = "30") long timeout) {
        log.info("Executing {} tasks with {} second timeout", tasks, timeout);
        parallelTaskService.logExecutorStats();

        List<String> results = parallelTaskService.executeWithTimeout(tasks, timeout);

        log.info("Completed {} out of {} tasks", results.size(), tasks);
        parallelTaskService.logExecutorStats();

        return ResponseEntity.ok(results);
    }

    /**
     * Get executor statistics
     * GET /api/parallel/status
     *
     * @return Executor status information
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        parallelTaskService.logExecutorStats();
        return ResponseEntity.ok("Check application logs for executor statistics");
    }
}

