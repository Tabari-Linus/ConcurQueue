package lii.concurqueuesystem;

import lii.concurqueuesystem.demo.ConcurrencyDemo;
import lii.concurqueuesystem.logging.ColoredConsoleFormatter;
import lii.concurqueuesystem.consumer.RetryWorker;
import lii.concurqueuesystem.consumer.TaskWorker;
import lii.concurqueuesystem.enums.ProducerStrategy;
import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.model.Task;
import lii.concurqueuesystem.monitor.SystemMonitor;
import lii.concurqueuesystem.producer.TaskProducer;
import lii.concurqueuesystem.util.DisplayFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConcurQueueSystemApplication {
    private static final Logger logger = Logger.getLogger(ConcurQueueSystemApplication.class.getName());

    private static final int WORKER_POOL_SIZE = 5;
    private static final int RETRY_WORKER_COUNT = 2;
    private static final int QUEUE_CAPACITY = 50;

    private final BlockingQueue<Task> taskQueue;
    private final BlockingQueue<Task> retryQueue;
    private final ThreadPoolExecutor workerPool;
    private final ExecutorService retryWorkerPool;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;

    private final AtomicBoolean shutdown;
    private final AtomicInteger tasksProcessed;
    private final AtomicLong totalProcessingTime;

    private final List<Thread> producerThreads;
    private Thread monitorThread;

    public ConcurQueueSystemApplication() {

        this.taskQueue = new PriorityBlockingQueue<>(QUEUE_CAPACITY);
        this.retryQueue = new LinkedBlockingQueue<>();

        this.workerPool = new ThreadPoolExecutor(
                WORKER_POOL_SIZE,
                WORKER_POOL_SIZE,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "TaskWorker-" + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                }
        );

        this.retryWorkerPool = Executors.newFixedThreadPool(RETRY_WORKER_COUNT, r -> {
            Thread t = new Thread(r, "RetryWorker-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });


        this.taskStatusMap = new ConcurrentHashMap<>();
        this.shutdown = new AtomicBoolean(false);
        this.tasksProcessed = new AtomicInteger(0);
        this.totalProcessingTime = new AtomicLong(0);
        this.producerThreads = new ArrayList<>();
    }


    public void start() {
        logger.info("Starting ConcurQueue system...");

        setupLogging();

        startWorkers();

        startRetryWorkers();

        startProducers();

        startMonitor();

        setupShutdownHook();

        logger.info("ConcurQueue system started successfully!");
        logger.info("System will run for 2 minutes, then demonstrate concurrency issues...");
    }

    private void setupLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        for (var handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new ColoredConsoleFormatter());

        rootLogger.addHandler(consoleHandler);
    }

    private void startWorkers() {
        logger.info("Starting worker threads...");

        for (int i = 0; i < WORKER_POOL_SIZE; i++) {
            workerPool.submit(new TaskWorker(
                    taskQueue,
                    retryQueue,
                    taskStatusMap,
                    tasksProcessed,
                    totalProcessingTime
            ));
        }

        logger.info(String.format("Started %d worker threads", WORKER_POOL_SIZE));
    }

    private void startRetryWorkers() {
        logger.info("Starting retry workers...");

        for (int i = 0; i < RETRY_WORKER_COUNT; i++) {
            retryWorkerPool.submit(new RetryWorker(retryQueue, taskQueue));
        }

        logger.info(String.format("Started %d retry workers", RETRY_WORKER_COUNT));
    }

    private void startProducers() {
        logger.info("Starting producer threads...");
        
        Thread highPriorityProducer = new Thread(new TaskProducer(
                "HighPriorityProducer",
                taskQueue,
                taskStatusMap,
                shutdown,
                ProducerStrategy.HIGH_PRIORITY_FOCUSED,
                3,
                3000
        ));
        highPriorityProducer.setDaemon(false);
        producerThreads.add(highPriorityProducer);

        Thread balancedProducer = new Thread(new TaskProducer(
                "BalancedProducer",
                taskQueue,
                taskStatusMap,
                shutdown,
                ProducerStrategy.BALANCED,
                5,
                4000
        ));
        balancedProducer.setDaemon(false);
        producerThreads.add(balancedProducer);

        Thread batchProducer = new Thread(new TaskProducer(
                "BatchProducer",
                taskQueue,
                taskStatusMap,
                shutdown,
                ProducerStrategy.LOW_PRIORITY_BATCH,
                8,
                6000
        ));
        batchProducer.setDaemon(false);
        producerThreads.add(batchProducer);

        for (Thread producer : producerThreads) {
            producer.start();
        }

        logger.info(String.format("Started %d producer threads", producerThreads.size()));
    }

    private void startMonitor() {
        logger.info("Starting system monitor...");

        monitorThread = new Thread(new SystemMonitor(
                taskQueue,
                retryQueue,
                workerPool,
                taskStatusMap,
                shutdown,
                tasksProcessed,
                totalProcessingTime
        ));
        monitorThread.setDaemon(true);
        monitorThread.start();

        logger.info("System monitor started");
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered - initiating graceful shutdown...");
            shutdown();
        }));
    }

    public void shutdown() {
        logger.info("Initiating system shutdown...");

        shutdown.set(true);


        logger.info("Stopping producers...");
        for (Thread producer : producerThreads) {
            producer.interrupt();
        }


        for (Thread producer : producerThreads) {
            try {
                producer.join(2000);
            } catch (InterruptedException e) {
                logger.warning("Interrupted while waiting for producer to shutdown");
                Thread.currentThread().interrupt();
            }
        }

        logger.info("Processing remaining tasks...");
        drainQueue();

        shutdownThreadPools();

        if (monitorThread != null) {
            monitorThread.interrupt();
        }

        printFinalStatistics();

        logger.info("ConcurQueue system shutdown complete");
    }

    private void drainQueue() {
        logger.info(String.format("Draining queue with %d remaining tasks...", taskQueue.size()));

        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warning("Worker pool did not terminate within timeout");
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.warning("Interrupted while waiting for worker pool termination");
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void shutdownThreadPools() {
        logger.info("Shutting down thread pools...");

        retryWorkerPool.shutdown();
        try {
            if (!retryWorkerPool.awaitTermination(10, TimeUnit.SECONDS)) {
                retryWorkerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            retryWorkerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void printFinalStatistics() {
        var statusCounts = taskStatusMap.values().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        status -> status.name(),
                        java.util.stream.Collectors.counting()));

        String finalDisplay = DisplayFormatter.createFinalStatisticsDisplay(
                tasksProcessed.get(),
                tasksProcessed.get() > 0 ? (double) totalProcessingTime.get() / tasksProcessed.get() : 0.0,
                taskQueue.size(),
                retryQueue.size(),
                taskStatusMap.size(),
                statusCounts
        );

        System.out.print(finalDisplay);

        logger.info("=== FINAL SYSTEM STATISTICS ===");
        logger.info(String.format("Total tasks processed: %d", tasksProcessed.get()));
        logger.info(String.format("Average processing time: %.2f ms",
                tasksProcessed.get() > 0 ?
                        (double) totalProcessingTime.get() / tasksProcessed.get() : 0.0));
        logger.info(String.format("Remaining tasks in queue: %d", taskQueue.size()));
        logger.info(String.format("Tasks in retry queue: %d", retryQueue.size()));
        logger.info(String.format("Total tasks tracked: %d", taskStatusMap.size()));

        logger.info("Task status breakdown:");
        for (var entry : statusCounts.entrySet()) {
            logger.info(String.format("  %s: %d", entry.getKey(), entry.getValue()));
        }
        logger.info("===============================");
    }

    public static void main(String[] args) {
        if (args.length > 0 && "direct".equals(args[0])) {
            logger.info("ConcurQueue - Multithreaded Job Processing Platform");
            logger.info("===================================================");

            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();

            try {
                logger.info("System running... (Press Ctrl+C to shutdown)");
                Thread.sleep(120000);

                logger.info("\nDemonstrating concurrency concepts...");
                ConcurrencyDemo.runAllDemonstrations();

            } catch (InterruptedException e) {
                logger.info("Main thread interrupted");
                Thread.currentThread().interrupt();
            } finally {
                system.shutdown();
            }

            logger.info("Application terminated");
        } else {
            lii.concurqueuesystem.menu.Menu.main(args);
        }
    }
}
