package io.casehub.soc.worker.contract;

import java.time.Instant;

public record RecoveryVerificationOutput(String status, Instant startedAt, String tenancyId) {}
