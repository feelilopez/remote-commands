package com.example.remotecommands.service;

import com.example.remotecommands.model.ExecutionJob;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ExecutionService {

    private final Map<UUID, ExecutionJob> jobs = new ConcurrentHashMap<>();
    private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();

    public ExecutionJob submit(String script, int cpuCount) {
        UUID id = UUID.randomUUID();
        ExecutionJob job = new ExecutionJob(id, script, cpuCount);
        jobs.put(id, job);
        queue.offer(id);
        return job;
    }

    public Optional<ExecutionJob> findById(UUID id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public Optional<ExecutionJob> nextQueuedJob() {
        UUID id = queue.poll();
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jobs.get(id));
    }

    public Collection<ExecutionJob> allJobs() {
        return jobs.values();
    }
}
