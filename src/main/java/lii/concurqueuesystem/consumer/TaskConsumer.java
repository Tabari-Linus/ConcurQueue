package lii.concurqueuesystem.consumer;

import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.exception.TaskProcessingException;
import lii.concurqueuesystem.model.Task;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class TaskConsumer implements Runnable {

    private static final Logger logger = Logger.getLogger(TaskConsumer.class.getName());
    private static final int MAX_RETRIES = 3;
    private static final double FAILURE_PROBABILITY = 0.15;

    private final BlockingQueue<Task> taskQueue;
    private final BlockingQueue<Task> retryQueue;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;
    private final AtomicInteger tasksProcessed;
    private final AtomicLong totalProcessingTime;
    private final Random random;
    private final String workerName;

    public TaskConsumer(BlockingQueue<Task> taskQueue,
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
        logger.info(String.format("Worker %s started", workerName));

    }

}
