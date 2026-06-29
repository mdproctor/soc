package io.casehub.soc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_alert")
public class SecurityAlertEntity {

    @Id
    @Column(name = "alert_id")
    private UUID alertId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "source_alert_id")
    private String sourceAlertId;

    @Column(name = "alert_timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "raw_payload", columnDefinition = "text")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    @Column(name = "category")
    private String category;

    @Column(name = "affected_assets")
    private String affectedAssets;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "tenancy_id", nullable = false)
    private String tenancyId;

    protected SecurityAlertEntity() {}

    public static SecurityAlertEntity from(SecurityAlert alert, String tenancyId) {
        var entity = new SecurityAlertEntity();
        entity.alertId = alert.alertId();
        entity.source = alert.source();
        entity.sourceAlertId = alert.sourceAlertId();
        entity.timestamp = alert.timestamp();
        entity.rawPayload = alert.rawPayload();
        entity.severity = alert.severity();
        entity.category = alert.category();
        entity.affectedAssets = alert.affectedAssets().isEmpty()
                ? null
                : String.join(",", alert.affectedAssets());
        entity.description = alert.description();
        entity.receivedAt = Instant.now();
        entity.tenancyId = tenancyId;
        return entity;
    }

    public SecurityAlert toDomain() {
        return new SecurityAlert(
                alertId, source, sourceAlertId, timestamp, rawPayload,
                severity, category,
                affectedAssets == null
                        ? java.util.List.of()
                        : java.util.List.of(affectedAssets.split(",")),
                description);
    }

    public UUID getAlertId() { return alertId; }
    public String getSource() { return source; }
    public AlertSeverity getSeverity() { return severity; }
    public Instant getTimestamp() { return timestamp; }
    public Instant getReceivedAt() { return receivedAt; }
    public String getTenancyId() { return tenancyId; }
}
