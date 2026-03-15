package com.example.remotecommands.model;

import java.time.Instant;
import java.util.UUID;

public class ExecutionJob {
    private final UUID id;
    private final String script;
    private final int cpuCount;
    private final Instant createdAt;

    private volatile Instant updatedAt;
    private volatile ExecutionStatus status;
    private volatile String output;

    public ExecutionJob(UUID id, String script, int cpuCount) {
        this.id = id;
        this.script = script;
        this.cpuCount = cpuCount;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.status = ExecutionStatus.QUEUED;
    }

    public UUID getId() {
        return id;
    }

    public String getScript() {
        return script;
    }

    public int getCpuCount() {
        return cpuCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public String getOutput() {
        return output;
    }

    public synchronized void markInProgress() {
        this.status = ExecutionStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public synchronized void markFinished(String output) {
        this.status = ExecutionStatus.FINISHED;
        this.updatedAt = Instant.now();
        this.output = output;
    }
}
