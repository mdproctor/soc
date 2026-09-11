package io.casehub.soc.worker;

import io.casehub.worker.api.Worker;
import io.casehub.worker.api.WorkerResult;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuleRecoveryVerificationWorker {

    private RuleRecoveryVerificationWorker() {}

    public static Worker create() {
        return Worker.builder()
                .name("rule-recovery-verification-start")
                .capabilityName("recovery-verification-start")
                .function((Map<String, Object> input) -> {
                    @SuppressWarnings("unchecked")
                    var execution = (Map<String, Object>) input.getOrDefault(
                            "containmentExecution", Map.of());

                    if (!Boolean.TRUE.equals(execution.get("executed"))) {
                        return skipResult();
                    }

                    String caseId = (String) input.get("caseId");
                    if (caseId == null) {
                        return skipResult();
                    }
                    String tenancyId = "case:" + caseId;

                    var output = new LinkedHashMap<String, Object>();
                    output.put("status", "started");
                    output.put("startedAt", Instant.now().toString());
                    output.put("tenancyId", tenancyId);

                    return WorkerResult.of(output);
                })
                .build();
    }

    private static WorkerResult skipResult() {
        var output = new LinkedHashMap<String, Object>();
        output.put("status", "skipped");
        output.put("startedAt", (Object) null);
        output.put("tenancyId", (Object) null);
        return WorkerResult.of(output);
    }
}
