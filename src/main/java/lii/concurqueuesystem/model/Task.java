package lii.concurqueuesystem.model;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


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

    public Task(Task original) {
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

    public void incrementRetryCount() {
        this.retryCount++;
    }

    @Override
    public int compareTo(Task other) {
        int priorityComparison = Integer.compare(other.priority, this.priority);
        if (priorityComparison != 0) {
            return priorityComparison;
        }
        return this.createdTimestamp.compareTo(other.createdTimestamp);
    }

    @Override
    public String toString() {
        return String.format("Task{id=%s, name='%s', priority=%d, retries=%d, created=%s}",
                id.toString().substring(0, 8), name, priority, retryCount, createdTimestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
