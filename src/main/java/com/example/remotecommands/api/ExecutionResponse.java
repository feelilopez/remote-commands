package com.example.remotecommands.api;

import com.example.remotecommands.model.ExecutionJob;
import com.example.remotecommands.model.ExecutionStatus;

import java.time.Instant;
import java.util.UUID;

public class ExecutionResponse {
    private final UUID id;
    private final ExecutionStatus status;
    private final int cpuCount;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String output;

    public ExecutionResponse(UUID id, ExecutionStatus status, int cpuCount, Instant createdAt, Instant updatedAt,
            String output) {
        this.id = id;
        this.status = status;
        this.cpuCount = cpuCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.output = output;
    }

    public static ExecutionResponse from(ExecutionJob job) {
        return new ExecutionResponse(
                job.getId(),
                job.getStatus(),
                job.getCpuCount(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                job.getOutput());
    }

    public UUID getId() {
        return id;
    }

    public ExecutionStatus getStatus() {
        return status;
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

    public String getOutput() {
        return output;
    }
}
