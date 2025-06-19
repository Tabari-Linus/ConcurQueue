package lii.concurqueuesystem.logging;

import java.util.logging.Logger;

public class TaskLogger {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    private final Logger logger;

    public TaskLogger(Class<?> clazz) {
        this.logger = Logger.getLogger(clazz.getName());
    }

    public void logTaskSuccess(String workerName, String taskName, long processingTime) {
        logger.info(String.format("%s✓ Worker %s completed task %s in %d ms%s",
                GREEN, workerName, taskName, processingTime, RESET));
    }

    public void logTaskFailure(String workerName, String taskName, String reason) {
        logger.warning(String.format("%s✗ Worker %s failed to process task %s: %s%s",
                RED, workerName, taskName, reason, RESET));
    }

    public void logTaskRetry(String taskName, int retryCount, int maxRetries) {
        logger.warning(String.format("%s⟲ Task %s queued for retry (attempt %d/%d)%s",
                YELLOW, taskName, retryCount, maxRetries, RESET));
    }

    public void logTaskAbandoned(String taskName, int maxRetries) {
        logger.severe(String.format("%s⚠ Task %s abandoned after %d retry attempts%s",
                RED, taskName, maxRetries, RESET));
    }

    public void logTaskProcessing(String workerName, String taskName) {
        logger.info(String.format("Worker %s processing task: %s", workerName, taskName));
    }

    public void logTaskQueued(String producerName, String taskName) {
        logger.info(String.format("Producer %s submitted task: %s", producerName, taskName));
    }

    public void logSystemEvent(String message) {
        logger.info(String.format("%sℹ %s%s", BLUE, message, RESET));
    }

    public void logSystemWarning(String message) {
        logger.warning(String.format("%s⚠ %s%s", YELLOW, message, RESET));
    }

    public void logSystemError(String message) {
        logger.severe(String.format("%s⚠ %s%s", RED, message, RESET));
    }

    public void info(String message) {
        logger.info(String.format("%sℹ %s%s", BLUE, message, RESET));
    }

    public void severe(String raceConditionDemoInterrupted) {
        logger.severe(String.format("%s⚠ %s%s", RED, raceConditionDemoInterrupted, RESET));
    }

    public void warning(String s) {
        logger.warning(String.format("%s⚠ %s%s", YELLOW, s, RESET));
    }
}
