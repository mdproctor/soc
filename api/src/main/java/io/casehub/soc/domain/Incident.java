package io.casehub.soc.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * A confirmed security threat — promoted from one or more {@link SecurityAlert}s after triage.
 * Maps to a {@code CaseInstance} in the engine.
 */
public record Incident(
        UUID incidentId,
        String title,
        IncidentStatus status,
        IncidentPriority priority,
        AlertSeverity severity,
        List<UUID> triggeringAlertIds,
        List<Ioc> iocs,
        List<AttackTechnique> attackTechniques,
        String assignedAnalyst,
        Instant createdAt,
        Instant updatedAt) {

    public Incident {
        Objects.requireNonNull(incidentId, "incidentId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(createdAt, "createdAt");
        triggeringAlertIds = triggeringAlertIds == null ? List.of() : List.copyOf(triggeringAlertIds);
        iocs = iocs == null ? List.of() : List.copyOf(iocs);
        attackTechniques = attackTechniques == null ? List.of() : List.copyOf(attackTechniques);
    }
}
