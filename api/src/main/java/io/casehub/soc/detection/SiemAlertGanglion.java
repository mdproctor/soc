package io.casehub.soc.detection;

import io.casehub.ras.api.DetectionResult;
import io.casehub.ras.api.JavaSwitchGanglion;
import io.casehub.ras.api.SituationContext;
import io.casehub.soc.domain.AlertSeverity;
import io.cloudevents.CloudEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Classifies incoming SIEM and EDR alerts by severity. Reads the {@code alertseverity}
 * CloudEvent extension (normalised by the webhook adapter) and maps to a detection signal.
 *
 * <p>Severity mapping:
 * <ul>
 *   <li>CRITICAL → DETECTED / 0.95</li>
 *   <li>HIGH → DETECTED / 0.80</li>
 *   <li>MEDIUM → WEAK / 0.50</li>
 *   <li>LOW → WEAK / 0.20</li>
 *   <li>INFORMATIONAL or missing → NOISE</li>
 * </ul>
 */
public class SiemAlertGanglion extends JavaSwitchGanglion {

    public static final String GANGLION_ID = "siem-alert-classifier";

    static final String EXT_SEVERITY = "alertseverity";
    static final String EXT_SOURCE = "alertsource";
    static final String EXT_RULE = "alertrule";

    public static final Set<String> EVENT_TYPES = Set.of(
        "soc.alert.siem.crowdstrike",
        "soc.alert.siem.splunk",
        "soc.alert.siem.sentinel",
        "soc.alert.edr.crowdstrike",
        "soc.alert.edr.sentinelone",
        "soc.alert.edr.carbonblack"
    );

    public SiemAlertGanglion() {
        super(GANGLION_ID, EVENT_TYPES);
    }

    @Override
    protected DetectionResult evaluate(CloudEvent event, SituationContext context) {
        AlertSeverity severity = extractSeverity(event);
        Map<String, Object> evidence = extractEvidence(event);

        return switch (severity) {
            case CRITICAL -> detected(0.95, evidence);
            case HIGH -> detected(0.80, evidence);
            case MEDIUM -> weak(0.50, evidence);
            case LOW -> weak(0.20, evidence);
            case INFORMATIONAL -> noise();
        };
    }

    private AlertSeverity extractSeverity(CloudEvent event) {
        Object raw = event.getExtension(EXT_SEVERITY);
        if (raw == null) return AlertSeverity.INFORMATIONAL;
        try {
            return AlertSeverity.valueOf(raw.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AlertSeverity.INFORMATIONAL;
        }
    }

    private Map<String, Object> extractEvidence(CloudEvent event) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        evidence.put("eventType", event.getType());
        Object source = event.getExtension(EXT_SOURCE);
        if (source != null) evidence.put("alertSource", source.toString());
        Object rule = event.getExtension(EXT_RULE);
        if (rule != null) evidence.put("alertRule", rule.toString());
        return Map.copyOf(evidence);
    }
}
