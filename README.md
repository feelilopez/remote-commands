# Remote Commands Demo (Java)

Minimal demo service to execute shell scripts on a remote executor (friend's laptop) via SSH + Docker.

Note: this is intentionally optimized for a clear demo, not production readiness.

## What this demo does

- Accepts a script and CPU count.
- Stores execution in memory.
- Processes jobs in background.
- Runs the script on a remote executor.
- Exposes status as: `QUEUED`, `IN_PROGRESS`, `FINISHED`.

## Architecture (simple)

1. Client submits execution via HTTP.
2. API stores the job in an in-memory queue.
3. Background worker picks next queued job every second.
4. Worker calls the executor.
5. The executor starts a Docker container on the remote machine and runs the script.
6. Worker stores output and marks job as finished.

## Tech stack

- Java 21
- Spring Boot 3
- Maven
- In-memory storage (`ConcurrentHashMap` + queue)
- Remote execution via `ssh` + Docker

## Prerequisites

- JDK 21
- Maven 3.9+
- SSH access to your friend's laptop (or another remote Linux host)
- Docker installed on that remote machine

## Configuration

Default config in `src/main/resources/application.yml`:

Notes:
- The remote user must be able to run Docker commands.
- The app binds to localhost by default (`127.0.0.1`).

For friend-laptop remote demo, set:

```bash
export EXECUTOR_SSH_HOST=<friend-laptop-host-or-ip>
export EXECUTOR_SSH_USER=<friend-user>
export EXECUTOR_SSH_PRIVATE_KEY=<path-to-private-key>
export EXECUTOR_DOCKER_IMAGE=alpine:3.20
```

Quick check before starting app:

```bash
ssh -i <path-to-private-key> <friend-user>@<friend-laptop-host-or-ip> "docker ps"
```

## Run

```bash
mvn spring-boot:run
```

## API

### 1) Submit execution

`POST /api/executions`

Request:

```json
{
  "script": "echo hello && sleep 2 && echo done",
  "cpuCount": 1
}
```

Response (201):

```json
{
  "id": "2a0bd008-e6f0-468f-b5de-c8c84ef4fe6e",
  "status": "QUEUED",
  "cpuCount": 1,
  "createdAt": "2026-03-15T12:00:00Z",
  "updatedAt": "2026-03-15T12:00:00Z",
  "output": null
}
```

### 2) Get execution status

`GET /api/executions/{id}`

Response example while running:

```json
{
  "id": "2a0bd008-e6f0-468f-b5de-c8c84ef4fe6e",
  "status": "IN_PROGRESS",
  "cpuCount": 1,
  "createdAt": "2026-03-15T12:00:00Z",
  "updatedAt": "2026-03-15T12:00:02Z",
  "output": null
}
```

Response example after completion:

```json
{
  "id": "2a0bd008-e6f0-468f-b5de-c8c84ef4fe6e",
  "status": "FINISHED",
  "cpuCount": 1,
  "createdAt": "2026-03-15T12:00:00Z",
  "updatedAt": "2026-03-15T12:00:05Z",
  "output": "EXECUTOR_READY\nhello\ndone\n"
}
```

## Quick demo flow (curl)

```bash
# 1) submit
curl -s -X POST http://localhost:8080/api/executions \
  -H 'Content-Type: application/json' \
  -d '{"script":"echo hello && sleep 2 && echo done","cpuCount":1}'

# 2) poll status (replace <id>)
curl -s http://localhost:8080/api/executions/<id>
```
