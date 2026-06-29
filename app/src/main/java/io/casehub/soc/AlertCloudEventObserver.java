package io.casehub.soc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.casehub.soc.domain.AlertSeverity;
import io.casehub.soc.domain.SecurityAlert;
import io.casehub.soc.domain.SecurityAlertEntity;
import io.casehub.soc.domain.SecurityAlertReceived;
import io.casehub.soc.domain.SecurityAlertRepository;
import io.cloudevents.CloudEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

@ApplicationScoped
public class AlertCloudEventObserver {

    private static final Logger LOG = Logger.getLogger(AlertCloudEventObserver.class);
    private static final String CE_TYPE_PREFIX = "soc.alert.";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SecurityAlertRepository repository;

    @Inject
    Event<SecurityAlertReceived> alertReceivedEvent;

    void onCloudEvent(@ObservesAsync CloudEvent event) {
        if (event.getType() == null || !event.getType().startsWith(CE_TYPE_PREFIX)) {
            return;
        }

        if (event.getData() == null) {
            LOG.warnf("CloudEvent %s has no data payload", event.getId());
            return;
        }

        try {
            byte[] bytes = event.getData().toBytes();
            JsonNode node = objectMapper.readTree(new String(bytes, StandardCharsets.UTF_8));

            String source = extractSource(event);
            SecurityAlert alert = new SecurityAlert(
                    UUID.randomUUID(),
                    source,
                    node.path("sourceAlertId").asText(null),
                    event.getTime() != null ? event.getTime().toInstant() : Instant.now(),
                    new String(bytes, StandardCharsets.UTF_8),
                    parseSeverity(node.path("severity").asText("MEDIUM")),
                    node.path("category").asText(null),
                    parseAffectedAssets(node),
                    node.path("description").asText(null));

            String tenancyId = event.getExtension("tenancyid") != null
                    ? event.getExtension("tenancyid").toString()
                    : "default";
            var entity = SecurityAlertEntity.from(alert, tenancyId);
            repository.persist(entity);
            alertReceivedEvent.fire(new SecurityAlertReceived(alert));

            LOG.infof("Ingested alert %s from CloudEvent %s (type=%s)", alert.alertId(), event.getId(), event.getType());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to process CloudEvent %s (type=%s)", event.getId(), event.getType());
        }
    }

    private String extractSource(CloudEvent event) {
        String ceType = event.getType();
        if (ceType.startsWith(CE_TYPE_PREFIX)) {
            String suffix = ceType.substring(CE_TYPE_PREFIX.length());
            int dot = suffix.indexOf('.');
            return dot > 0 ? suffix.substring(dot + 1) : suffix;
        }
        return event.getSource() != null ? event.getSource().toString() : "unknown";
    }

    private AlertSeverity parseSeverity(String raw) {
        try {
            return AlertSeverity.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AlertSeverity.MEDIUM;
        }
    }

    private List<String> parseAffectedAssets(JsonNode node) {
        JsonNode assets = node.path("affectedAssets");
        if (!assets.isArray()) return List.of();
        List<String> result = new ArrayList<>();
        assets.forEach(n -> result.add(n.asText()));
        return result;
    }
}
