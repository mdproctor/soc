package io.casehub.soc.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A raw security alert received from an external detection system (SIEM, EDR, IDS).
 * This is the triggering event that may or may not become an incident after triage.
 *
 * <p>Pure-Java record — no framework dependencies. Persisted via JPA entity in {@code app/}.
 */
public record SecurityAlert(
        UUID alertId,
        String source,
        String sourceAlertId,
        Instant timestamp,
        String rawPayload,
        AlertSeverity severity,
        String category,
        List<String> affectedAssets,
        String description) {

    public SecurityAlert {
        Objects.requireNonNull(alertId, "alertId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(timestamp, "timestamp");
        Objects.requireNonNull(severity, "severity");
        affectedAssets = affectedAssets == null ? List.of() : List.copyOf(affectedAssets);
    }
}
