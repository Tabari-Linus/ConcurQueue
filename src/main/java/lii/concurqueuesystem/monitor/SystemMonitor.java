package lii.concurqueuesystem.monitor;

import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.model.Task;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

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
