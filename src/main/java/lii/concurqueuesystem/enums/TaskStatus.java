package lii.concurqueuesystem.enums;

import lombok.Data;
import lombok.Getter;

@Getter
public enum TaskStatus {
    SUBMITTED("Task has been submitted to queue"),
    PROCESSING("Task is currently being processed by a worker"),
    COMPLETED("Task has been completed successfully"),
    FAILED("Task processing failed"),
    RETRY("Task failed but will be retried"),
    ABANDONED("Task exceeded maximum retry attempts");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name() + " - " + description;
    }
}
