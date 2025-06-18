package lii.concurqueuesystem.model;

import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Data
@Getter
public class Task implements Comparable<Task>{

    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private Instant lastProcessedTimestamp;

    public Task(String name, int priority, String payload) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
        this.retryCount = 0;
    }

    private Task(Task original) {
        this.id = original.id;
        this.name = original.name;
        this.priority = original.priority;
        this.createdTimestamp = original.createdTimestamp;
        this.payload = original.payload;
        this.retryCount = original.retryCount + 1;
        this.lastProcessedTimestamp = original.lastProcessedTimestamp;
    }

    public Task createRetry() {
        if (retryCount >= MAX_RETRIES) {
            throw new IllegalStateException("Max retries exceeded for task: " + id);
        }
        return new Task(this);
    }

    public boolean canRetry() {
        return retryCount < MAX_RETRIES;
    }

    @Override
    public int compareTo(Task other) {
        int priorityComparison = Integer.compare(this.priority, other.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        return this.createdTimestamp.compareTo(other.createdTimestamp);
    }
}
