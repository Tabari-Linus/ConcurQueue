package lii.concurqueuesystem.monitor;

import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.model.Task;

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


}
