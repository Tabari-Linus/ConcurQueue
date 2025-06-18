package lii.concurqueuesystem.consumer;

import lii.concurqueuesystem.model.Task;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class RetryConsumer implements Runnable {

    private static final Logger logger = Logger.getLogger(RetryConsumer.class.getName());
    private static final long RETRY_DELAY_MS = 2000;

    private final BlockingQueue<Task> retryQueue;
    private final BlockingQueue<Task> mainQueue;
    private final String workerName;

    public RetryConsumer(BlockingQueue<Task> retryQueue, BlockingQueue<Task> mainQueue) {
        this.retryQueue = retryQueue;
        this.mainQueue = mainQueue;
        this.workerName = Thread.currentThread().getName();
    }

    @Override
    public void run() {
        logger.info(String.format("Retry worker %s started", workerName));

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Task retryTask = retryQueue.take();

                Thread.sleep(RETRY_DELAY_MS);

                mainQueue.put(retryTask);

                logger.info(String.format("Retry worker %s requeued task %s for retry attempt %d",
                        workerName, retryTask.getName(), retryTask.getRetryCount()));

            } catch (InterruptedException e) {
                logger.info(String.format("Retry worker %s interrupted", workerName));
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info(String.format("Retry worker %s shutting down", workerName));
    }
}
