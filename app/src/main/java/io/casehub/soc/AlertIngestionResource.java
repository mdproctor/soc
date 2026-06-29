package io.casehub.soc;

import io.casehub.platform.api.identity.TenancyConstants;
import io.casehub.soc.domain.AlertSeverity;
import io.casehub.soc.domain.SecurityAlert;
import io.casehub.soc.domain.SecurityAlertEntity;
import io.casehub.soc.domain.SecurityAlertReceived;
import io.casehub.soc.domain.SecurityAlertRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
@Path("/api/alerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AlertIngestionResource {

    @Inject
    SecurityAlertRepository repository;

    @Inject
    Event<SecurityAlertReceived> alertReceivedEvent;

    @POST
    public Response ingest(AlertPayload payload) {
        var alert = payload.toDomain();
        var entity = SecurityAlertEntity.from(alert, TenancyConstants.DEFAULT_TENANT_ID);

        repository.persist(entity);
        alertReceivedEvent.fire(new SecurityAlertReceived(alert));

        return Response.accepted(Map.of("alertId", alert.alertId())).build();
    }

    public record AlertPayload(
            String source,
            String sourceAlertId,
            String severity,
            String category,
            List<String> affectedAssets,
            String description,
            String rawPayload) {

        SecurityAlert toDomain() {
            return new SecurityAlert(
                    UUID.randomUUID(),
                    source,
                    sourceAlertId,
                    Instant.now(),
                    rawPayload,
                    AlertSeverity.valueOf(severity.toUpperCase()),
                    category,
                    affectedAssets,
                    description);
        }
    }
}
