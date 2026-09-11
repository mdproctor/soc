package io.casehub.soc.worker.contract;

import java.time.Instant;
import java.util.List;

public record RecoveryVerificationResultOutput(
    String status,
    List<String> verifiedNodes,
    List<String> divergedNodes,
    Instant completedAt,
    long executionToVerificationMs
) {}
