package com.dragonfruit.core.experiments;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {
    private final ClockSyncService clockSync;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    public ExperimentController(ClockSyncService clockSync) {
        this.clockSync = clockSync;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return List.of(
            Map.of("id", 1, "name", "RPC and RMI", "status", "IMPLEMENTATION_REQUIRED"),
            Map.of("id", 2, "name", "Multithreading", "status", "DEMO_AVAILABLE"),
            Map.of("id", 3, "name", "Clock Synchronization", "status", "DEMO_AVAILABLE"),
            Map.of("id", 6, "name", "Data Consistency and Replication", "status", "DEMO_AVAILABLE"),
            Map.of("id", 8, "name", "MapReduce Job", "status", "DEMO_AVAILABLE")
        );
    }

    @PostMapping("/2/run")
    public Map<String, Object> multithreading(@RequestParam(defaultValue = "20") int tasks)
        throws InterruptedException {
        if (tasks < 1 || tasks > 500) {
            throw new IllegalArgumentException("tasks must be between 1 and 500");
        }
        long start = System.nanoTime();
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            final int taskId = i + 1;
            futures.add(CompletableFuture.supplyAsync(() -> Map.of(
                "taskId", taskId,
                "thread", Thread.currentThread().getName(),
                "completedAt", Instant.now().toString()
            ), executor));
        }
        List<Map<String, Object>> results = futures.stream().map(CompletableFuture::join).toList();
        return Map.of("experiment", 2, "tasksRequested", tasks,
            "tasksCompleted", results.size(),
            "elapsedMillis", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
            "results", results);
    }

    @PostMapping("/3/clock-sync")
    public Map<String, Object> clockSync(@RequestParam long sendEpochMillis,
                                         @RequestParam long receiveEpochMillis,
                                         @RequestParam long serverEpochMillis) {
        return clockSync.estimate(sendEpochMillis, receiveEpochMillis, serverEpochMillis);
    }

    @PostMapping("/6/consistency-demo")
    public Map<String, Object> consistency(@RequestParam(defaultValue = "SYNCHRONOUS") String mode) {
        String normalized = mode.toUpperCase(Locale.ROOT);
        if (!Set.of("SYNCHRONOUS", "ASYNCHRONOUS").contains(normalized)) {
            throw new IllegalArgumentException("mode must be SYNCHRONOUS or ASYNCHRONOUS");
        }
        return Map.of(
            "experiment", 6,
            "mode", normalized,
            "note", "This endpoint describes the selected mode; real replica writes and convergence are implemented by the cluster module.",
            "acknowledgementPolicy", normalized.equals("SYNCHRONOUS")
                ? "Acknowledge after required replicas confirm"
                : "Acknowledge after local write; replicas may temporarily lag",
            "timestamp", Instant.now().toString()
        );
    }

    @PostMapping("/8/mapreduce")
    public Map<String, Object> mapReduce(@RequestBody List<Map<String, Object>> rows) {
        if (rows == null || rows.size() > 10000) {
            throw new IllegalArgumentException("Provide between 0 and 10000 rows");
        }
        Map<String, Long> mapped = new TreeMap<>();
        for (Map<String, Object> row : rows) {
            Object category = row.get("category");
            Object quantity = row.get("quantity");
            if (category == null || !(quantity instanceof Number)) {
                throw new IllegalArgumentException("Each row requires category and numeric quantity");
            }
            mapped.merge(category.toString(), ((Number) quantity).longValue(), Long::sum);
        }
        return Map.of("experiment", 8, "inputRows", rows.size(),
            "mapOutputGroups", mapped.size(), "reduceResult", mapped);
    }
}
