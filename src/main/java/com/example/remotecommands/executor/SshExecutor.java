package com.example.remotecommands.executor;

import com.example.remotecommands.model.ExecutionJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.Base64;

@Component
public class SshExecutor {

    @Value("${executor.ssh.host}")
    private String host;

    @Value("${executor.ssh.user}")
    private String user;

    @Value("${executor.ssh.private-key}")
    private String privateKeyPath;

    @Value("${executor.docker.image:alpine:3.20}")
    private String dockerImage;

    public String execute(ExecutionJob job) {
        try {
            Process process = new ProcessBuilder(
                    "ssh",
                    "-i", privateKeyPath,
                    "-o", "StrictHostKeyChecking=no",
                    user + "@" + host,
                    "bash -s").start();

            String remoteScript = buildRemoteScript(job);
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(remoteScript.getBytes(StandardCharsets.UTF_8));
            }

            ByteArrayOutputStream combined = new ByteArrayOutputStream();
            copyStream(process.getInputStream(), combined);
            copyStream(process.getErrorStream(), combined);

            boolean finished = process.waitFor(10, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                return "Execution timed out";
            }

            String output = combined.toString(StandardCharsets.UTF_8);
            if (process.exitValue() != 0) {
                return "Remote command failed\n" + output;
            }
            return output;
        } catch (Exception ex) {
            return "Execution failed: " + ex.getMessage();
        }
    }

    private String buildRemoteScript(ExecutionJob job) {
        String encodedUserScript = Base64.getEncoder()
                .encodeToString(job.getScript().getBytes(StandardCharsets.UTF_8));
        String containerName = "exec-" + job.getId().toString().replace("-", "");

        StringBuilder sb = new StringBuilder();
        sb.append("set -e\n");
        sb.append("WORKDIR=$(mktemp -d)\n");
        sb.append("echo '").append(encodedUserScript).append("' | base64 -d > \"$WORKDIR/user-script.sh\"\n");
        sb.append("chmod +x \"$WORKDIR/user-script.sh\"\n\n");

        sb.append("docker rm -f ").append(containerName).append(" >/dev/null 2>&1 || true\n");
        sb.append("docker create --name ").append(containerName)
                .append(" --cpus ").append(job.getCpuCount())
                .append(" -v \"$WORKDIR:/work\" ")
                .append(dockerImage)
                .append(" sh -c \"echo EXECUTOR_READY; sh /work/user-script.sh\" >/dev/null\n\n");

        sb.append("docker start ").append(containerName).append(" >/dev/null\n\n");

        sb.append("for i in $(seq 1 20); do\n");
        sb.append("  if docker logs ").append(containerName).append(" 2>&1 | grep -q EXECUTOR_READY; then\n");
        sb.append("    break\n");
        sb.append("  fi\n");
        sb.append("  sleep 1\n");
        sb.append("done\n\n");

        sb.append("docker wait ").append(containerName).append(" >/dev/null\n");
        sb.append("docker logs ").append(containerName).append(" 2>&1\n");
        sb.append("docker rm -f ").append(containerName).append(" >/dev/null\n");
        sb.append("rm -rf \"$WORKDIR\"\n");

        return sb.toString();
    }

    private void copyStream(java.io.InputStream in, ByteArrayOutputStream out) throws java.io.IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
