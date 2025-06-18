package lii.concurqueuesystem.monitor;

import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.model.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SystemMonitor implements Runnable {

    private static final Logger logger = Logger.getLogger(SystemMonitor.class.getName());
    private static final long MONITOR_INTERVAL_MS = 5000;
    private static final long EXPORT_INTERVAL_MS = 60000;

    private final BlockingQueue<Task> taskQueue;
    private final BlockingQueue<Task> retryQueue;
    private final ThreadPoolExecutor workerPool;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;
    private final AtomicBoolean shutdown;
    private final AtomicInteger tasksProcessed;
    private final AtomicLong totalProcessingTime;

    private long lastExportTime;
    private final AtomicInteger monitorCycles;

    public SystemMonitor(BlockingQueue<Task> taskQueue,
                         BlockingQueue<Task> retryQueue,
                         ThreadPoolExecutor workerPool,
                         ConcurrentHashMap<String, TaskStatus> taskStatusMap,
                         AtomicBoolean shutdown,
                         AtomicInteger tasksProcessed,
                         AtomicLong totalProcessingTime) {
        this.taskQueue = taskQueue;
        this.retryQueue = retryQueue;
        this.workerPool = workerPool;
        this.taskStatusMap = taskStatusMap;
        this.shutdown = shutdown;
        this.tasksProcessed = tasksProcessed;
        this.totalProcessingTime = totalProcessingTime;
        this.lastExportTime = System.currentTimeMillis();
        this.monitorCycles = new AtomicInteger(0);
    }

    @Override
    public void run() {
        logger.info("System monitor started");
    }

    private boolean shouldExportToJson() {
        return (System.currentTimeMillis() - lastExportTime) >= EXPORT_INTERVAL_MS;
    }

    private void exportTaskStatusToJson() {
        try {
            SystemMetrics metrics = collectMetrics();
            String json = generateJsonReport(metrics);

            String filename = String.format("concur_queue_status_%d.json", System.currentTimeMillis());
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(json);
            }

            logger.info("Task status exported to: " + filename);

        } catch (IOException e) {
            logger.severe("Failed to export task status to JSON: " + e.getMessage());
        }
    }

    private SystemMetrics collectMetrics() {
        SystemMetrics metrics = new SystemMetrics();

        metrics.mainQueueSize = taskQueue.size();
        metrics.retryQueueSize = retryQueue.size();

        metrics.activeThreads = workerPool.getActiveCount();
        metrics.corePoolSize = workerPool.getCorePoolSize();
        metrics.maximumPoolSize = workerPool.getMaximumPoolSize();
        metrics.completedTaskCount = workerPool.getCompletedTaskCount();

        Map<TaskStatus, Long> statusCounts = taskStatusMap.values().stream()
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));

        metrics.submittedCount = statusCounts.getOrDefault(TaskStatus.SUBMITTED, 0L).intValue();
        metrics.processingCount = statusCounts.getOrDefault(TaskStatus.PROCESSING, 0L).intValue();
        metrics.completedCount = statusCounts.getOrDefault(TaskStatus.COMPLETED, 0L).intValue();
        metrics.failedCount = statusCounts.getOrDefault(TaskStatus.FAILED, 0L).intValue();
        metrics.retryCount = statusCounts.getOrDefault(TaskStatus.RETRY, 0L).intValue();
        metrics.abandonedCount = statusCounts.getOrDefault(TaskStatus.ABANDONED, 0L).intValue();

        metrics.totalProcessed = tasksProcessed.get();
        metrics.averageProcessingTime = metrics.totalProcessed > 0 ?
                (double) totalProcessingTime.get() / metrics.totalProcessed : 0.0;

        return metrics;
    }

    private String generateJsonReport(SystemMetrics metrics) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(Instant.now().toString()).append("\",\n");
        json.append("  \"monitor_cycle\": ").append(monitorCycles.get()).append(",\n");
        json.append("  \"queues\": {\n");
        json.append("    \"main_queue_size\": ").append(metrics.mainQueueSize).append(",\n");
        json.append("    \"retry_queue_size\": ").append(metrics.retryQueueSize).append("\n");
        json.append("  },\n");
        json.append("  \"thread_pool\": {\n");
        json.append("    \"active_threads\": ").append(metrics.activeThreads).append(",\n");
        json.append("    \"core_pool_size\": ").append(metrics.corePoolSize).append(",\n");
        json.append("    \"maximum_pool_size\": ").append(metrics.maximumPoolSize).append(",\n");
        json.append("    \"completed_task_count\": ").append(metrics.completedTaskCount).append("\n");
        json.append("  },\n");
        json.append("  \"task_status\": {\n");
        json.append("    \"submitted\": ").append(metrics.submittedCount).append(",\n");
        json.append("    \"processing\": ").append(metrics.processingCount).append(",\n");
        json.append("    \"completed\": ").append(metrics.completedCount).append(",\n");
        json.append("    \"failed\": ").append(metrics.failedCount).append(",\n");
        json.append("    \"retry\": ").append(metrics.retryCount).append(",\n");
        json.append("    \"abandoned\": ").append(metrics.abandonedCount).append("\n");
        json.append("  },\n");
        json.append("  \"performance\": {\n");
        json.append("    \"total_processed\": ").append(metrics.totalProcessed).append(",\n");
        json.append("    \"average_processing_time_ms\": ").append(String.format("%.2f", metrics.averageProcessingTime)).append("\n");
        json.append("  }\n");
        json.append("}\n");

        return json.toString();
    }


    private static class SystemMetrics {
        int mainQueueSize;
        int retryQueueSize;
        int activeThreads;
        int corePoolSize;
        int maximumPoolSize;
        long completedTaskCount;
        int submittedCount;
        int processingCount;
        int completedCount;
        int failedCount;
        int retryCount;
        int abandonedCount;
        int totalProcessed;
        double averageProcessingTime;
    }

}
