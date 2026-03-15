package com.example.remotecommands.service;

import com.example.remotecommands.executor.SshExecutor;
import com.example.remotecommands.model.ExecutionJob;
import com.example.remotecommands.model.ExecutionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExecutionWorker {

    private final ExecutionService executionService;
    private final SshExecutor sshExecutor;

    public ExecutionWorker(ExecutionService executionService, SshExecutor sshExecutor) {
        this.executionService = executionService;
        this.sshExecutor = sshExecutor;
    }

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {
        ExecutionJob job = executionService.nextQueuedJob().orElse(null);
        if (job == null || job.getStatus() != ExecutionStatus.QUEUED) {
            return;
        }

        job.markInProgress();
        String output = sshExecutor.execute(job);
        job.markFinished(output);
    }
}
