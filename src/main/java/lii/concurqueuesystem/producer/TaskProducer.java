package lii.concurqueuesystem.producer;

import lii.concurqueuesystem.enums.ProducerStrategy;
import lii.concurqueuesystem.enums.TaskStatus;
import lii.concurqueuesystem.model.Task;
import lombok.Getter;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
@Getter
public class TaskProducer implements Runnable {

    private static final Logger logger = Logger.getLogger(TaskProducer.class.getName());

    private final String producerName;
    private final BlockingQueue<Task> taskQueue;
    private final ConcurrentHashMap<String, TaskStatus> taskStatusMap;
    private final AtomicBoolean shutdown;
    private final AtomicInteger tasksProduced;

    private final Random random;
    private final ProducerStrategy strategy;
    private final int tasksPerBatch;
    private final long intervalMillis;

    public TaskProducer(String producerName,
                        BlockingQueue<Task> taskQueue,
                        ConcurrentHashMap<String, TaskStatus> taskStatusMap,
                        AtomicBoolean shutdown,
                        ProducerStrategy strategy,
                        int tasksPerBatch,
                        long intervalMillis) {
        this.producerName = producerName;
        this.taskQueue = taskQueue;
        this.taskStatusMap = taskStatusMap;
        this.shutdown = shutdown;
        this.strategy = strategy;
        this.tasksPerBatch = tasksPerBatch;
        this.intervalMillis = intervalMillis;
        this.random = new Random();
        this.tasksProduced = new AtomicInteger(0);
    }

    @Override
    public void run() {
        logger.info(String.format("Producer %s started with strategy %s",
                producerName, strategy));

        while (!shutdown.get()) {
            try {
                produceBatch();
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                logger.info(String.format("Producer %s interrupted", producerName));
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.severe(String.format("Producer %s encountered error: %s",
                        producerName, e.getMessage()));
            }
        }

        logger.info(String.format("Producer %s shutting down. Total tasks produced: %d",
                producerName, tasksProduced.get()));
    }

    private void produceBatch() throws InterruptedException {
        for (int i = 0; i < tasksPerBatch; i++) {
            Task task = generateTask();

            taskQueue.put(task);

            taskStatusMap.put(task.getId().toString(), TaskStatus.SUBMITTED);

            tasksProduced.incrementAndGet();

            logger.info(String.format("Producer %s submitted task: %s",
                    producerName, task));
        }
    }

    private Task generateTask() {
        int taskNumber = tasksProduced.get() + 1;
        String taskName = String.format("%s-Task-%d", producerName, taskNumber);

        int priority = strategy.generatePriority(random);
        String payload = strategy.generatePayload(random, taskNumber);

        return new Task(taskName, priority, payload);
    }

    public int getTasksProduced() {
        return tasksProduced.get();
    }

}
