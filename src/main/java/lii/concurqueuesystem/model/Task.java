package lii.concurqueuesystem.model;

import java.time.Instant;
import java.util.UUID;

public class Task implements Comparable<Task>{

    private final UUID id;
    private final String name;
    private final int priority;
    private final Instant createdTimestamp;
    private final String payload;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public Task(String name, int priority, String payload) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.priority = priority;
        this.createdTimestamp = Instant.now();
        this.payload = payload;
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
