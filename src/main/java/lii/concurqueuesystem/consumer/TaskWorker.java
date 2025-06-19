package lii.concurqueuesystem.consumer;

import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.exception.TaskProcessingException;
import lii.concurqueuesystem.logging.TaskLogger;
import lii.concurqueuesystem.model.Task;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TaskWorker implements Runnable {

    private static final TaskLogger taskLogger = new TaskLogger(TaskWorker.class);
    private static final int MAX_RETRIES = 3;
    private static final double FAILURE_PROBABILITY = 0.15;

    private final BlockingQueue<Task> taskQueue;
    private final BlockingQueue<Task> retryQueue;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;
    private final AtomicInteger tasksProcessed;
    private final AtomicLong totalProcessingTime;
    private final Random random;
    private final String workerName;

    public TaskWorker(BlockingQueue<Task> taskQueue,
                      BlockingQueue<Task> retryQueue,
                      ConcurrentHashMap<String, TaskStatus> taskStatusMap,
                      AtomicInteger tasksProcessed,
                      AtomicLong totalProcessingTime) {
        this.taskQueue = taskQueue;
        this.retryQueue = retryQueue;
        this.taskStatusMap = taskStatusMap;
        this.tasksProcessed = tasksProcessed;
        this.totalProcessingTime = totalProcessingTime;
        this.random = new Random();
        this.workerName = Thread.currentThread().getName();
    }

    @Override
    public void run() {
        taskLogger.logSystemEvent(String.format("Worker %s started", workerName));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task task = taskQueue.take();
                processTask(task);

            } catch (InterruptedException e) {
                taskLogger.logSystemEvent(String.format("Worker %s interrupted", workerName));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                taskLogger.logSystemError(String.format("Worker %s encountered unexpected error: %s",
                        workerName, e.getMessage()));
            }
        }

        taskLogger.logSystemEvent(String.format("Worker %s shutting down", workerName));
    }

    private void processTask(Task task) {
        String taskId = task.getId().toString();
        Instant startTime = Instant.now();

        try {
            taskStatusMap.put(taskId, TaskStatus.PROCESSING);
            taskLogger.logTaskProcessing(workerName, task.getName());

            long processingTime = calculateProcessingTime(task);
            Thread.sleep(processingTime);

            if (shouldSimulateFailure()) {
                throw new TaskProcessingException("Simulated processing failure");
            }

            taskStatusMap.put(taskId, TaskStatus.COMPLETED);
            tasksProcessed.incrementAndGet();

            long actualProcessingTime = Instant.now().toEpochMilli() - startTime.toEpochMilli();
            totalProcessingTime.addAndGet(actualProcessingTime);

            taskLogger.logTaskSuccess(workerName, task.getName(), actualProcessingTime);

        } catch (InterruptedException e) {
            taskLogger.logSystemEvent(String.format("Worker %s interrupted while processing task %s",
                    workerName, task.getName()));
            taskStatusMap.put(taskId, TaskStatus.FAILED);
            Thread.currentThread().interrupt();

        } catch (TaskProcessingException e) {
            handleTaskFailure(task, e);
        }
    }

    private void handleTaskFailure(Task task, Exception e) {
        String taskId = task.getId().toString();

        taskLogger.logTaskFailure(workerName, task.getName(), e.getMessage());

        if (task.getRetryCount() < MAX_RETRIES) {
            Task retryTask = new Task(task);
            taskStatusMap.put(taskId, TaskStatus.RETRY);

            try {
                retryQueue.put(retryTask);
                taskLogger.logTaskRetry(task.getName(), retryTask.getRetryCount(), MAX_RETRIES);
            } catch (InterruptedException ie) {
                taskLogger.logSystemError(String.format("Failed to queue retry for task %s", task.getName()));
                taskStatusMap.put(taskId, TaskStatus.FAILED);
                Thread.currentThread().interrupt();
            }
        } else {
            taskStatusMap.put(taskId, TaskStatus.ABANDONED);
            taskLogger.logTaskAbandoned(task.getName(), MAX_RETRIES);
        }
    }

    private long calculateProcessingTime(Task task) {
        int priority = task.getPriority();
        if (priority >= 8) {
            return 2000 + random.nextInt(3000);
        } else if (priority >= 4) {
            return 1000 + random.nextInt(2000);
        } else {
            return 500 + random.nextInt(1000);
        }
    }

    private boolean shouldSimulateFailure() {
        return random.nextDouble() < FAILURE_PROBABILITY;
    }
}