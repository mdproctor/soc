package io.casehub.soc.worker;

import io.casehub.worker.api.Worker;
import io.casehub.worker.api.WorkerResult;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SocRecoveryVerificationResultWorker {

    private SocRecoveryVerificationResultWorker() {}

    public static Worker create() {
        return Worker.builder()
                .name("rule-recovery-verification-result")
                .capabilityName("recovery-verification-result")
                .function((Map<String, Object> input) -> {
                    @SuppressWarnings("unchecked")
                    var verification = (Map<String, Object>) input.getOrDefault(
                            "recoveryVerification", Map.of());
                    @SuppressWarnings("unchecked")
                    var execution = (Map<String, Object>) input.getOrDefault(
                            "containmentExecution", Map.of());

                    String status = (String) verification.getOrDefault("status", "unknown");
                    @SuppressWarnings("unchecked")
                    var verifiedNodes = (List<String>) verification.getOrDefault("verifiedNodes", List.of());
                    @SuppressWarnings("unchecked")
                    var divergedNodes = (List<String>) verification.getOrDefault("divergedNodes", List.of());

                    long executionToVerificationMs = 0;
                    if (execution.get("executionTimestamp") != null
                            && verification.get("convergenceTime") != null) {
                        try {
                            Instant execTime = Instant.parse(execution.get("executionTimestamp").toString());
                            Instant convTime = Instant.parse(verification.get("convergenceTime").toString());
                            executionToVerificationMs = Duration.between(execTime, convTime).toMillis();
                        } catch (Exception ignored) {}
                    }

                    var output = new LinkedHashMap<String, Object>();
                    output.put("status", status);
                    output.put("verifiedNodes", verifiedNodes);
                    output.put("divergedNodes", divergedNodes);
                    output.put("completedAt", Instant.now().toString());
                    output.put("executionToVerificationMs", executionToVerificationMs);

                    return WorkerResult.of(output);
                })
                .build();
    }
}
