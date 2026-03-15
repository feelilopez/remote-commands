package com.example.remotecommands.api;

import com.example.remotecommands.service.ExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExecutionResponse submit(@Valid @RequestBody SubmitExecutionRequest request) {
        return ExecutionResponse.from(executionService.submit(request.getScript(), request.getCpuCount()));
    }

    @GetMapping("/{id}")
    public ExecutionResponse getById(@PathVariable UUID id) {
        return executionService.findById(id)
                .map(ExecutionResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Execution not found"));
    }
}
